package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.repository.entity.PublishEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.PublishEventEntity;

import java.util.List;

/**
 * 发布事件仓储
 *
 * @author ahoo wang
 */
public interface PublishEventRepository extends EventRepository, LeaderRepository {

    PublishIdentity initialize(String eventName, Object eventData);

    int markSucceeded(PublishIdentity publishIdentity) throws ConcurrentVersionConflictException;

    int markFailed(PublishIdentity publishIdentity, Throwable throwable) throws ConcurrentVersionConflictException;

    /**
     * @param limit
     * @param before     {@link java.util.concurrent.TimeUnit#MILLISECONDS}
     * @param maxVersion
     * @return
     */
    List<PublishEventEntity> queryFailed(int limit, long before, int maxVersion);

    int compensate(PublishEventCompensateEntity publishEventCompensationEntity);

}
