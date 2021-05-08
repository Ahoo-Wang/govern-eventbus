package me.ahoo.eventbus.jdbc;

import com.google.common.base.Throwables;
import lombok.var;
import me.ahoo.eventbus.core.repository.*;
import me.ahoo.eventbus.core.repository.entity.CompensateLeader;
import me.ahoo.eventbus.core.repository.entity.PublishEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.PublishEventEntity;
import me.ahoo.eventbus.core.serialize.Serializer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
public class JdbcPublishEventRepository implements PublishEventRepository {
    private final Serializer serializer;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcPublishEventRepository(Serializer serializer
            , NamedParameterJdbcTemplate jdbcTemplate) {
        this.serializer = serializer;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SQL_INITIALIZED
            = "insert publish_event (event_name, event_data, status,version,create_time) values (:event_name, :event_data, :status,:version,:create_time);";

    @Override
    public PublishIdentity initialize(String eventName, Object eventData) {
        var subscribeIdentity = new PublishIdentity();

        subscribeIdentity.setStatus(PublishStatus.INITIALIZED);
        subscribeIdentity.setVersion(Version.INITIAL_VALUE);
        subscribeIdentity.setEventName(eventName);
        subscribeIdentity.setCreateTime(System.currentTimeMillis());

        MapSqlParameterSource sqlParams = new MapSqlParameterSource("event_name", eventName);
        String eventDataStr = serializer.serialize(eventData);
        sqlParams.addValue("event_data", eventDataStr);
        sqlParams.addValue("status", subscribeIdentity.getStatus().getValue());
        sqlParams.addValue("version", subscribeIdentity.getVersion());
        sqlParams.addValue("create_time", subscribeIdentity.getCreateTime());
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(SQL_INITIALIZED, sqlParams, keyHolder);
        subscribeIdentity.setId(keyHolder.getKey().longValue());
        return subscribeIdentity;
    }


    private void checkAffected(PublishIdentity publishIdentity, int affected) {
        if (affected > 0) {
            return;
        }
        var errMsg = String.format("Publish [%s] mark [%d]@[%d] to status [%s] error."
                , publishIdentity.getEventName()
                , publishIdentity.getId()
                , publishIdentity.getVersion()
                , publishIdentity.getStatus().name());
        throw new ConcurrentVersionConflictException(errMsg, publishIdentity);
    }

    private static final String SQL_MARK_SUCCEEDED
            = "update publish_event set status=1,version=version+1,published_time=:published_time where id=:id and version=:version;";

    @Override
    public int markSucceeded(PublishIdentity publishIdentity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", publishIdentity.getId());
        sqlParams.addValue("version", publishIdentity.getVersion());
        sqlParams.addValue("published_time", System.currentTimeMillis());
        var affected = jdbcTemplate.update(SQL_MARK_SUCCEEDED, sqlParams);
        checkAffected(publishIdentity, affected);
        return affected;
    }


    private static final String SQL_MARK_FAILED
            = "update publish_event set status=2,version=version+1 where id=:id and version=:version;";

    /**
     * 1. first insert log
     * 2. last mark status to failed
     *
     * @param publishIdentity
     * @param throwable
     * @return
     */
    @Override
    public int markFailed(PublishIdentity publishIdentity, Throwable throwable) {

        insertPublishEventFailed(publishIdentity, throwable);

        MapSqlParameterSource sqlParams = new MapSqlParameterSource("id", publishIdentity.getId());
        sqlParams.addValue("version", publishIdentity.getVersion());

        var affected = jdbcTemplate.update(SQL_MARK_FAILED, sqlParams);
        checkAffected(publishIdentity, affected);
        return affected;
    }

    private static final String SQL_PUBLISH_FAILED
            = "insert publish_event_failed(publish_event_id, failed_msg,create_time) values (:publish_event_id, :failed_msg,:create_time)";

    private void insertPublishEventFailed(PublishIdentity publishIdentity, Throwable throwable) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("publish_event_id", publishIdentity.getId());
        var failedMsg = Throwables.getStackTraceAsString(throwable);
        sqlParams.addValue("failed_msg", failedMsg);
        sqlParams.addValue("create_time", System.currentTimeMillis());
        jdbcTemplate.update(SQL_PUBLISH_FAILED, sqlParams);
    }

    private static final String SQL_QUERY_FAILED
            = "select id, event_name, event_data, status, published_time, version, create_time from publish_event " +
            "where status<>1 and create_time<:before and version<:max_version order by version asc limit :limit;";

    @Override
    public List<PublishEventEntity> queryFailed(int limit, long before, int maxVersion) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("max_version", maxVersion);
        var beforeTime = System.currentTimeMillis() - before;
        sqlParams.addValue("before", beforeTime);
        sqlParams.addValue("limit", limit);
        return jdbcTemplate.query(SQL_QUERY_FAILED, sqlParams, (rs, rowNum) -> {
            var id = rs.getLong("id");
            var eventName = rs.getString("event_name");
            var eventDataStr = rs.getString("event_data");
            var status = rs.getInt("status");
            var publishedTime = rs.getLong("published_time");
            var version = rs.getInt("version");
            var createTime = rs.getLong("create_time");
            var entity = new PublishEventEntity();
            entity.setId(id);
            entity.setEventName(eventName);
            entity.setEventData(eventDataStr);
            entity.setStatus(PublishStatus.valueOf(status));
            entity.setVersion(version);
            entity.setPublishedTime(publishedTime);
            entity.setCreateTime(createTime);
            return entity;
        });
    }

    private final static String SQL_COMPENSATE
            = "insert publish_event_compensate (publish_event_id, start_time, taken, failed_msg) " +
            "values (:publish_event_id, :start_time, :taken, :failed_msg);";

    @Override
    public int compensate(PublishEventCompensateEntity publishEventCompensateEntity) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("publish_event_id", publishEventCompensateEntity.getPublishEventId());
        sqlParams.addValue("start_time", publishEventCompensateEntity.getStartTime());
        sqlParams.addValue("taken", publishEventCompensateEntity.getTaken());
        sqlParams.addValue("failed_msg", publishEventCompensateEntity.getFailedMsg());
        return jdbcTemplate.update(SQL_COMPENSATE, sqlParams);
    }


    /**
     * get current Leader
     *
     * @return
     */
    @Override
    public CompensateLeader getLeader() {
        return getLeader(this.jdbcTemplate, CompensateLeader.PUBLISH_LEADER);
    }

    private final static String SQL_GET_LEADER
            = "select name, term_start, term_end, transition_period, leader_id, version, unix_timestamp() as current_ts " +
            "from compensate_leader " +
            "where name = :name;";

    public static CompensateLeader getLeader(NamedParameterJdbcTemplate jdbcTemplate, String leaderName) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("name", leaderName);
        return jdbcTemplate.queryForObject(SQL_GET_LEADER, sqlParams, (rs, rowNum) -> {
            var id = rs.getString("name");
            var termStart = rs.getLong("term_start");
            var termEnd = rs.getLong("term_end");
            var transitionPeriod = rs.getLong("transition_period");
            var leaderId = rs.getString("leader_id");
            var version = rs.getInt("version");
            var currentTs = rs.getLong("current_ts");
            var entity = new CompensateLeader();
            entity.setName(id);
            entity.setTermStart(termStart);
            entity.setTermEnd(termEnd);
            entity.setTransitionPeriod(transitionPeriod);
            entity.setLeaderId(leaderId);
            entity.setVersion(version);
            entity.setCurrentTs(currentTs);
            return entity;
        });
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
        return fightLeadership(this.jdbcTemplate, CompensateLeader.PUBLISH_LEADER, termLength, transitionLength, leaderId, lastVersion);
    }

    /**
     * Release the leadership
     *
     * @param leaderId
     * @return
     */
    @Override
    public boolean releaseLeadership(String leaderId) {
        return releaseLeadership(this.jdbcTemplate, CompensateLeader.PUBLISH_LEADER, leaderId);
    }

    private static final String SQL_FIGHT_LEADERSHIP = "update compensate_leader set " +
            "term_start=unix_timestamp()," +
            "term_end=(unix_timestamp()+:term_length)," +
            "transition_period=(unix_timestamp()+:term_length+:transition_length)," +
            "leader_id=:leader_id," +
            "version=version + 1 " +
            "where name =:name and version = :last_version;";

    public static boolean fightLeadership(NamedParameterJdbcTemplate jdbcTemplate, String leaderName, long termLength, long transitionLength, String leaderId, int lastVersion) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("name", leaderName);
        sqlParams.addValue("term_length", termLength);
        sqlParams.addValue("transition_length", transitionLength);
        sqlParams.addValue("leader_id", leaderId);
        sqlParams.addValue("last_version", lastVersion);
        return jdbcTemplate.update(SQL_FIGHT_LEADERSHIP, sqlParams) > 0;
    }

    private static final String SQL_RELEASE_LEADERSHIP = "update compensate_leader set " +
            "term_start=0," +
            "term_end=0," +
            "transition_period=0," +
            "leader_id=''," +
            "version=version + 1 " +
            "where name =:name and leader_id = :leader_id;";

    public static boolean releaseLeadership(NamedParameterJdbcTemplate jdbcTemplate, String leaderName, String leaderId) {
        MapSqlParameterSource sqlParams = new MapSqlParameterSource("name", leaderName);
        sqlParams.addValue("leader_id", leaderId);
        return jdbcTemplate.update(SQL_RELEASE_LEADERSHIP, sqlParams) > 0;
    }


}
