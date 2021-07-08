/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.eventbus.jdbc;

import com.google.common.base.Throwables;
import lombok.var;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.*;
import me.ahoo.eventbus.core.repository.entity.CompensateLeader;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
public class JdbcSubscribeEventRepository implements SubscribeEventRepository {
    private final Serializer serializer;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSubscribeEventRepository(Serializer serializer
            , NamedParameterJdbcTemplate jdbcTemplate) {
        this.serializer = serializer;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SQL_GET_SUBSCRIBE_EVENT = "select id,status,version from subscribe_event " +
            "where event_id=:event_id and event_name=:event_name and subscribe_name=:subscribe_name limit 1;";

    private Optional<SubscribeIdentity> getSubscribeIdentity(Subscriber subscriber, Long eventId, String eventName) {
        MapSqlParameterSource getSubscribeEventParams = new MapSqlParameterSource("event_id", eventId);
        getSubscribeEventParams.addValue("event_name", eventName);
        getSubscribeEventParams.addValue("subscribe_name", subscriber.getName());
        try {
            SubscribeIdentity subscribeIdentity = jdbcTemplate.queryForObject(SQL_GET_SUBSCRIBE_EVENT, getSubscribeEventParams, (rs, rowNum) -> {
                var id = rs.getLong("id");
                var status = rs.getInt("status");
                var version = rs.getInt("version");
                var _subscribeIdentity = new SubscribeIdentity();
                _subscribeIdentity.setId(id);
                _subscribeIdentity.setSubscriberName(subscriber.getName());
                _subscribeIdentity.setStatus(SubscribeStatus.valeOf(status));
                _subscribeIdentity.setVersion(version);
                return _subscribeIdentity;
            });
            return Optional.of(subscribeIdentity);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private static final String SQL_SUBSCRIBE_INITIALIZED = "insert subscribe_event(subscribe_name, status,subscribe_time, event_id, event_name, event_data, event_create_time, version,create_time)" +
            "values (:subscribe_name, :status,:subscribe_time, :event_id, :event_name, :event_data, :event_create_time, :version,:create_time);";

    private SubscribeIdentity initializeSubscribeIdentity(Subscriber subscriber, PublishEvent subscribePublishEvent, String eventName) {
        var subscribeIdentity = new SubscribeIdentity();

        subscribeIdentity.setStatus(SubscribeStatus.INITIALIZED);
        subscribeIdentity.setVersion(Version.INITIAL_VALUE);
        subscribeIdentity.setSubscriberName(subscriber.getName());

        MapSqlParameterSource sqlParams = new MapSqlParameterSource("subscribe_name", subscriber.getName());
        sqlParams.addValue("status", subscribeIdentity.getStatus().getValue());
        sqlParams.addValue("subscribe_time", System.currentTimeMillis());
        sqlParams.addValue("event_id", subscribePublishEvent.getId());
        sqlParams.addValue("event_name", eventName);
        String eventData = serializer.serialize(subscribePublishEvent.getEventData());
        sqlParams.addValue("event_data", eventData);
        sqlParams.addValue("event_create_time", subscribePublishEvent.getCreateTime());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        sqlParams.addValue("create_time", System.currentTimeMillis());
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(SQL_SUBSCRIBE_INITIALIZED, sqlParams, keyHolder);
        subscribeIdentity.setId(keyHolder.getKey().longValue());
        return subscribeIdentity;
    }

    @Override
    public SubscribeIdentity initialize(Subscriber subscriber, PublishEvent subscribePublishEvent) throws RepeatedSubscribeException {
        String eventName = subscribePublishEvent.getEventName();
        var subscribeIdentity = getSubscribeIdentity(subscriber, subscribePublishEvent.getId(), eventName);
        if (subscribeIdentity.isPresent()) {
            if (subscribeIdentity.get().getStatus().equals(SubscribeStatus.SUCCEEDED)) {
                throw new RepeatedSubscribeException(subscriber, subscribePublishEvent);
            }
            return subscribeIdentity.get();
        }
        return initializeSubscribeIdentity(subscriber, subscribePublishEvent, eventName);
    }


    private void checkAffected(SubscribeIdentity subscribeIdentity, int affected) {
        if (affected == 0) {
            var errMsg = String.format("Subscribe [%s] mark id:[%d] on version:[%d] to status [%s] error."
                    , subscribeIdentity.getSubscriberName()
                    , subscribeIdentity.getId()
                    , subscribeIdentity.getVersion()
                    , subscribeIdentity.getStatus().name());
            throw new ConcurrentVersionConflictException(errMsg, subscribeIdentity);
        }
    }

    private static final String SQL_MARK_SUCCEEDED
            = "update subscribe_event set status=1,version=version+1 where id=:id and version=:version;";

    @Override
    public int markSucceeded(SubscribeIdentity subscribeIdentity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", subscribeIdentity.getId());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        var affected = jdbcTemplate.update(SQL_MARK_SUCCEEDED, sqlParams);
        checkAffected(subscribeIdentity, affected);
        return affected;
    }


    private static final String SQL_MARK_FAILED
            = "update subscribe_event set status=2,version=version+1 where id=:id and version=:version;";

    /**
     * 1. insert failed log
     * 2. mark failed status
     *
     * @param subscribeIdentity
     * @param throwable
     * @return
     */
    @Override
    public int markFailed(SubscribeIdentity subscribeIdentity, Throwable throwable) {

        insertSubscribeEventFailed(subscribeIdentity, throwable);

        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", subscribeIdentity.getId());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        var affected = jdbcTemplate.update(SQL_MARK_FAILED, sqlParams);
        checkAffected(subscribeIdentity, affected);
        return affected;
    }

    private static final String SQL_SUBSCRIBE_FAILED
            = "insert subscribe_event_failed (subscribe_event_id, failed_msg,create_time) values (:subscribe_event_id, :failed_msg,:create_time)";

    private void insertSubscribeEventFailed(SubscribeIdentity subscribeIdentity, Throwable throwable) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("subscribe_event_id", subscribeIdentity.getId());
        var failedMsg = Throwables.getStackTraceAsString(throwable);
        sqlParams.addValue("failed_msg", failedMsg);
        sqlParams.addValue("create_time", System.currentTimeMillis());
        jdbcTemplate.update(SQL_SUBSCRIBE_FAILED, sqlParams);
    }

    private static final String SQL_QUERY_FAILED
            = "select id, subscribe_name, status, subscribe_time, event_id, event_name, event_data, event_create_time, version, create_time from subscribe_event " +
            "where status<>1 and create_time<:before and version<:max_version order by version asc limit :limit;";

    /***
     *
     * @param limit
     * @param before {@link java.util.concurrent.TimeUnit#MILLISECONDS}
     * @param maxVersion
     * @return
     */
    @Override
    public List<SubscribeEventEntity> queryFailed(int limit, long before, int maxVersion) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("max_version", maxVersion);
        var beforeTime = System.currentTimeMillis() - before;
        sqlParams.addValue("before", beforeTime);
        sqlParams.addValue("limit", limit);
        return jdbcTemplate.query(SQL_QUERY_FAILED, sqlParams, (rs, rowNum) -> {
            var id = rs.getLong("id");
            var subscribeName = rs.getString("subscribe_name");
            var status = rs.getInt("status");
            var subscribeTime = rs.getLong("subscribe_time");
            var eventId = rs.getLong("event_id");
            var eventName = rs.getString("event_name");
            var eventDataStr = rs.getString("event_data");
            var eventCreateTime = rs.getLong("event_create_time");
            var version = rs.getInt("version");
            var createTime = rs.getLong("create_time");

            var entity = new SubscribeEventEntity();
            entity.setId(id);
            entity.setSubscriberName(subscribeName);
            entity.setStatus(SubscribeStatus.valeOf(status));
            entity.setSubscribeTime(subscribeTime);
            entity.setEventId(eventId);
            entity.setEventName(eventName);
            entity.setEventData(eventDataStr);

            entity.setEventCreateTime(eventCreateTime);
            entity.setVersion(version);
            entity.setCreateTime(createTime);
            return entity;
        });
    }

    private static String SQL_COMPENSATE
            = "insert subscribe_event_compensate (subscribe_event_id, start_time, taken, failed_msg) " +
            "values (:subscribe_event_id, :start_time, :taken, :failed_msg) ";

    @Override
    public int compensate(SubscribeEventCompensateEntity subscribeEventCompensateEntity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("subscribe_event_id", subscribeEventCompensateEntity.getSubscribeEventId());
        sqlParams.addValue("start_time", subscribeEventCompensateEntity.getStartTime());
        sqlParams.addValue("taken", subscribeEventCompensateEntity.getTaken());
        sqlParams.addValue("failed_msg", subscribeEventCompensateEntity.getFailedMsg());
        return jdbcTemplate.update(SQL_COMPENSATE, sqlParams);
    }

    /**
     * get current Leader
     *
     * @return
     */
    @Override
    public CompensateLeader getLeader() {
        return JdbcPublishEventRepository.getLeader(this.jdbcTemplate, CompensateLeader.SUBSCRIBE_LEADER);
    }

    /**
     * Fighting for leadership rights
     *
     * @param termLength       任期时长 {@link TimeUnit#SECONDS}
     * @param transitionLength 过度期时长  {@link TimeUnit#SECONDS}
     * @param leaderId
     * @param lastVersion
     * @return 1: success 2: failure
     */
    @Override
    public boolean fightLeadership(long termLength, long transitionLength, String leaderId, int lastVersion) {
        return JdbcPublishEventRepository.fightLeadership(this.jdbcTemplate, CompensateLeader.SUBSCRIBE_LEADER, termLength, transitionLength, leaderId, lastVersion);
    }

    /**
     * Release the leadership
     *
     * @param leaderId
     * @return
     */
    @Override
    public boolean releaseLeadership(String leaderId) {
        return JdbcPublishEventRepository.releaseLeadership(this.jdbcTemplate, CompensateLeader.SUBSCRIBE_LEADER, leaderId);
    }
}
