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

import me.ahoo.cosid.provider.LazyIdGenerator;
import me.ahoo.eventbus.core.repository.ConcurrentVersionConflictException;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.repository.PublishStatus;
import me.ahoo.eventbus.core.repository.Version;
import me.ahoo.eventbus.core.repository.entity.PublishEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.PublishEventEntity;
import me.ahoo.eventbus.core.serialize.Serializer;

import com.google.common.base.Throwables;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Duration;
import java.util.List;

/**
 * JdbcPublishEventRepository.
 *
 * @author ahoo wang
 */
public class JdbcPublishEventRepository implements PublishEventRepository {
    public static final String EVENT_BUS_ID_NAME = "eventbus";
    
    private final Serializer serializer;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LazyIdGenerator lazyIdGenerator;
    
    public JdbcPublishEventRepository(Serializer serializer, NamedParameterJdbcTemplate jdbcTemplate) {
        this.serializer = serializer;
        this.jdbcTemplate = jdbcTemplate;
        this.lazyIdGenerator = new LazyIdGenerator(EVENT_BUS_ID_NAME);
    }
    
    private static final String SQL_INITIALIZED
        = "insert publish_event (id,event_name, event_data_id,event_data, status,version,create_time) values (:id,:event_name,:event_data_id, :event_data, :status,:version,:create_time);";
    
    @Override
    public PublishIdentity initialize(String eventName, long eventDataId, Object eventData) {
        PublishIdentity publishIdentity = new PublishIdentity();
        publishIdentity.setId(lazyIdGenerator.generate());
        publishIdentity.setStatus(PublishStatus.INITIALIZED);
        publishIdentity.setVersion(Version.INITIAL_VALUE);
        publishIdentity.setEventName(eventName);
        publishIdentity.setEventDataId(eventDataId);
        publishIdentity.setCreateTime(System.currentTimeMillis());
        
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("event_name", eventName);
        String eventDataStr = serializer.serialize(eventData);
        sqlParams.addValue("id", publishIdentity.getId());
        sqlParams.addValue("event_data_id", publishIdentity.getEventDataId());
        sqlParams.addValue("event_data", eventDataStr);
        sqlParams.addValue("status", publishIdentity.getStatus().getValue());
        sqlParams.addValue("version", publishIdentity.getVersion());
        sqlParams.addValue("create_time", publishIdentity.getCreateTime());
        jdbcTemplate.update(SQL_INITIALIZED, sqlParams);
        return publishIdentity;
    }
    
    
    private void checkAffected(PublishIdentity publishIdentity, int affected) {
        if (affected > 0) {
            return;
        }
        String errMsg = String.format("Publish [%s] mark [%d]@[%d] to status [%s] error.",
            publishIdentity.getEventName(),
            publishIdentity.getId(),
            publishIdentity.getVersion(),
            publishIdentity.getStatus().name());
        throw new ConcurrentVersionConflictException(errMsg, publishIdentity);
    }
    
    private static final String SQL_MARK_SUCCEEDED
        = "update publish_event set status=1,version=version+1,published_time=:published_time where id=:id and version=:version and create_time=:create_time;";
    
    @Override
    public int markSucceeded(PublishIdentity publishIdentity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", publishIdentity.getId());
        sqlParams.addValue("version", publishIdentity.getVersion());
        sqlParams.addValue("published_time", System.currentTimeMillis());
        sqlParams.addValue("create_time", publishIdentity.getCreateTime());
        
        int affected = jdbcTemplate.update(SQL_MARK_SUCCEEDED, sqlParams);
        checkAffected(publishIdentity, affected);
        return affected;
    }
    
    
    private static final String SQL_MARK_FAILED
        = "update publish_event set status=2,version=version+1 where id=:id and version=:version and create_time=:create_time;";
    
    /**
     * markFailed.
     * <pre>
     *     1. first insert log
     *     2. last mark status to failed
     * </pre>
     */
    @Override
    public int markFailed(PublishIdentity publishIdentity, Throwable throwable) {
        
        insertPublishEventFailed(publishIdentity, throwable);
        
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", publishIdentity.getId());
        sqlParams.addValue("version", publishIdentity.getVersion());
        sqlParams.addValue("create_time", publishIdentity.getCreateTime());
        int affected = jdbcTemplate.update(SQL_MARK_FAILED, sqlParams);
        checkAffected(publishIdentity, affected);
        return affected;
    }
    
    private static final String SQL_PUBLISH_FAILED
        = "insert publish_event_failed(publish_event_id, failed_msg,create_time) values (:publish_event_id, :failed_msg,:create_time)";
    
    private void insertPublishEventFailed(PublishIdentity publishIdentity, Throwable throwable) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("publish_event_id", publishIdentity.getId());
        String failedMsg = Throwables.getStackTraceAsString(throwable);
        sqlParams.addValue("failed_msg", failedMsg);
        sqlParams.addValue("create_time", System.currentTimeMillis());
        jdbcTemplate.update(SQL_PUBLISH_FAILED, sqlParams);
    }
    
    private static final String SQL_QUERY_FAILED
        = "select id, event_name,event_data_id, event_data, status, published_time, version, create_time from publish_event "
        + "where status<>1 and create_time between :lower and :upper and version<:max_version order by version asc limit :limit;";
    
    @Override
    public List<PublishEventEntity> queryFailed(int limit, int maxVersion, Duration before, Duration range) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("max_version", maxVersion);
        long upperTime = System.currentTimeMillis() - before.toMillis();
        long lowerTime = upperTime - range.toMillis();
        sqlParams.addValue("lower", lowerTime);
        sqlParams.addValue("upper", upperTime);
        sqlParams.addValue("limit", limit);
        return jdbcTemplate.query(SQL_QUERY_FAILED, sqlParams, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String eventName = rs.getString("event_name");
            long eventDataId = rs.getLong("event_data_id");
            String eventDataStr = rs.getString("event_data");
            int status = rs.getInt("status");
            long publishedTime = rs.getLong("published_time");
            int version = rs.getInt("version");
            long createTime = rs.getLong("create_time");
            PublishEventEntity entity = new PublishEventEntity();
            entity.setId(id);
            entity.setEventName(eventName);
            entity.setEventDataId(eventDataId);
            entity.setEventData(eventDataStr);
            entity.setStatus(PublishStatus.valueOf(status));
            entity.setVersion(version);
            entity.setPublishedTime(publishedTime);
            entity.setCreateTime(createTime);
            return entity;
        });
    }
    
    private static final String SQL_COMPENSATE
        = "insert publish_event_compensate (publish_event_id, start_time, taken, failed_msg) "
        + "values (:publish_event_id, :start_time, :taken, :failed_msg);";
    
    @Override
    public int compensate(PublishEventCompensateEntity publishEventCompensateEntity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("publish_event_id", publishEventCompensateEntity.getPublishEventId());
        sqlParams.addValue("start_time", publishEventCompensateEntity.getStartTime());
        sqlParams.addValue("taken", publishEventCompensateEntity.getTaken());
        sqlParams.addValue("failed_msg", publishEventCompensateEntity.getFailedMsg());
        return jdbcTemplate.update(SQL_COMPENSATE, sqlParams);
    }
    
}
