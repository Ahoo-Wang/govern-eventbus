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
import me.ahoo.cosid.provider.IdGeneratorProvider;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.*;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * @author ahoo wang
 */
public class JdbcSubscribeEventRepository implements SubscribeEventRepository {

    private final Serializer serializer;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final IdGeneratorProvider idGeneratorProvider;

    public JdbcSubscribeEventRepository(Serializer serializer
            , NamedParameterJdbcTemplate jdbcTemplate, IdGeneratorProvider idGeneratorProvider) {
        this.serializer = serializer;
        this.jdbcTemplate = jdbcTemplate;
        this.idGeneratorProvider = idGeneratorProvider;
    }

    private static final String SQL_GET_SUBSCRIBE_EVENT = "select id,status,version from subscribe_event " +
            "where event_id=:event_id and event_name=:event_name and subscribe_name=:subscribe_name and event_create_time=:event_create_time limit 1;";

    private Optional<SubscribeIdentity> getSubscribeIdentity(Subscriber subscriber, PublishEvent subscribePublishEvent) {
        MapSqlParameterSource getSubscribeEventParams = new MapSqlParameterSource("event_id", subscribePublishEvent.getId());
        getSubscribeEventParams.addValue("event_name", subscribePublishEvent.getEventName());
        getSubscribeEventParams.addValue("subscribe_name", subscriber.getName());
        getSubscribeEventParams.addValue("event_create_time", subscribePublishEvent.getCreateTime());
        try {
            SubscribeIdentity subscribeIdentity = jdbcTemplate.queryForObject(SQL_GET_SUBSCRIBE_EVENT, getSubscribeEventParams, (rs, rowNum) -> {
                long id = rs.getLong("id");
                int status = rs.getInt("status");
                int version = rs.getInt("version");
                SubscribeIdentity _subscribeIdentity = new SubscribeIdentity();
                _subscribeIdentity.setId(id);
                _subscribeIdentity.setSubscriberName(subscriber.getName());
                _subscribeIdentity.setStatus(SubscribeStatus.valueOf(status));
                _subscribeIdentity.setVersion(version);
                _subscribeIdentity.setEventCreateTime(subscribePublishEvent.getCreateTime());
                return _subscribeIdentity;
            });
            return Optional.of(subscribeIdentity);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private static final String SQL_SUBSCRIBE_INITIALIZED = "insert subscribe_event(id,subscribe_name, status,subscribe_time, event_id, event_name, event_data_id,event_data, event_create_time, version,create_time)" +
            "values (:id,:subscribe_name, :status,:subscribe_time, :event_id, :event_name, :event_data_id,:event_data, :event_create_time, :version,:create_time);";

    private SubscribeIdentity initializeSubscribeIdentity(Subscriber subscriber, PublishEvent subscribePublishEvent, String eventName) {
        SubscribeIdentity subscribeIdentity = new SubscribeIdentity();
        subscribeIdentity.setId(JdbcPublishEventRepository.generateId(idGeneratorProvider));
        subscribeIdentity.setStatus(SubscribeStatus.INITIALIZED);
        subscribeIdentity.setVersion(Version.INITIAL_VALUE);
        subscribeIdentity.setSubscriberName(subscriber.getName());
        subscribeIdentity.setEventCreateTime(subscribePublishEvent.getCreateTime());

        MapSqlParameterSource sqlParams = new MapSqlParameterSource("subscribe_name", subscriber.getName());
        sqlParams.addValue("id", subscribeIdentity.getId());
        sqlParams.addValue("status", subscribeIdentity.getStatus().getValue());
        sqlParams.addValue("subscribe_time", System.currentTimeMillis());
        sqlParams.addValue("event_id", subscribePublishEvent.getId());
        sqlParams.addValue("event_name", eventName);
        sqlParams.addValue("event_data_id", subscribePublishEvent.getEventDataId());
        String eventData = serializer.serialize(subscribePublishEvent.getEventData());
        sqlParams.addValue("event_data", eventData);
        sqlParams.addValue("event_create_time", subscribeIdentity.getEventCreateTime());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        sqlParams.addValue("create_time", System.currentTimeMillis());

        jdbcTemplate.update(SQL_SUBSCRIBE_INITIALIZED, sqlParams);
        return subscribeIdentity;
    }

    @Override
    public SubscribeIdentity initialize(Subscriber subscriber, PublishEvent subscribePublishEvent) throws RepeatedSubscribeException {
        String eventName = subscribePublishEvent.getEventName();
        Optional<SubscribeIdentity> subscribeIdentity = getSubscribeIdentity(subscriber, subscribePublishEvent);
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
            String errMsg = String.format("Subscribe [%s] mark id:[%d] on version:[%d] to status [%s] error."
                    , subscribeIdentity.getSubscriberName()
                    , subscribeIdentity.getId()
                    , subscribeIdentity.getVersion()
                    , subscribeIdentity.getStatus().name());
            throw new ConcurrentVersionConflictException(errMsg, subscribeIdentity);
        }
    }

    private static final String SQL_MARK_SUCCEEDED
            = "update subscribe_event set status=1,version=version+1 where id=:id and version=:version and event_create_time=:event_create_time limit 1;";

    @Override
    public int markSucceeded(SubscribeIdentity subscribeIdentity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", subscribeIdentity.getId());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        sqlParams.addValue("event_create_time", subscribeIdentity.getEventCreateTime());
        int affected = jdbcTemplate.update(SQL_MARK_SUCCEEDED, sqlParams);
        checkAffected(subscribeIdentity, affected);
        return affected;
    }


    private static final String SQL_MARK_FAILED
            = "update subscribe_event set status=2,version=version+1 where id=:id and version=:version and event_create_time=:event_create_time limit 1;";

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
        sqlParams.addValue("event_create_time", subscribeIdentity.getEventCreateTime());
        int affected = jdbcTemplate.update(SQL_MARK_FAILED, sqlParams);
        checkAffected(subscribeIdentity, affected);
        return affected;
    }

    private static final String SQL_SUBSCRIBE_FAILED
            = "insert subscribe_event_failed (subscribe_event_id, failed_msg,create_time) values (:subscribe_event_id, :failed_msg,:create_time)";

    private void insertSubscribeEventFailed(SubscribeIdentity subscribeIdentity, Throwable throwable) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("subscribe_event_id", subscribeIdentity.getId());
        String failedMsg = Throwables.getStackTraceAsString(throwable);
        sqlParams.addValue("failed_msg", failedMsg);
        sqlParams.addValue("create_time", System.currentTimeMillis());
        jdbcTemplate.update(SQL_SUBSCRIBE_FAILED, sqlParams);
    }

    private static final String SQL_QUERY_FAILED
            = "select id, subscribe_name, status, subscribe_time, event_id, event_name,event_data_id, event_data, event_create_time, version, create_time from subscribe_event " +
            "where status<>1 and event_create_time between :lower and :upper and version<:max_version order by version asc limit :limit;";

    /***
     *
     * @param limit
     * @param maxVersion
     * @param before
     * @param range
     * @return
     */
    @Override
    public List<SubscribeEventEntity> queryFailed(int limit, int maxVersion, Duration before, Duration range) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("max_version", maxVersion);
        long upperTime = System.currentTimeMillis() - before.toMillis();
        long lowerTime = upperTime - range.toMillis();
        sqlParams.addValue("lower", lowerTime);
        sqlParams.addValue("upper", upperTime);
        sqlParams.addValue("limit", limit);
        return jdbcTemplate.query(SQL_QUERY_FAILED, sqlParams, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String subscribeName = rs.getString("subscribe_name");
            int status = rs.getInt("status");
            long subscribeTime = rs.getLong("subscribe_time");
            long eventId = rs.getLong("event_id");
            String eventName = rs.getString("event_name");
            long eventDataId = rs.getLong("event_data_id");
            String eventDataStr = rs.getString("event_data");
            long eventCreateTime = rs.getLong("event_create_time");
            int version = rs.getInt("version");
            long createTime = rs.getLong("create_time");

            SubscribeEventEntity entity = new SubscribeEventEntity();
            entity.setId(id);
            entity.setSubscriberName(subscribeName);
            entity.setStatus(SubscribeStatus.valueOf(status));
            entity.setSubscribeTime(subscribeTime);
            entity.setEventId(eventId);
            entity.setEventName(eventName);
            entity.setEventDataId(eventDataId);
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
}
