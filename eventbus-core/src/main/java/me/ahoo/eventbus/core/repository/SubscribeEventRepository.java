package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import java.util.List;

/**
 * 订阅事件仓储
 *
 * @author ahoo wang
 */
public interface SubscribeEventRepository extends EventRepository, LeaderRepository {

    /**
     * *  getSubscribeEventBy event_id and event_name and subscribe_name
     * *  if exist
     * *      status is SUCCEEDED throw error
     * *      status is INITIALIZED return
     * *  else insert subscribe event:INITIALIZED to local db
     *
     * @param subscriber            Subscriber
     * @param subscribePublishEvent PublishEventWrapper
     * @return SubscribeIdentity
     */
    SubscribeIdentity initialize(Subscriber subscriber, PublishEvent subscribePublishEvent) throws RepeatedSubscribeException;


    /**
     * subscribeEventId and version
     *
     * @param subscribeIdentity SubscribeIdentity
     * @return Returns the number of affected rows
     */
    int markSucceeded(SubscribeIdentity subscribeIdentity) throws ConcurrentVersionConflictException;

    /**
     * subscribeEventId and version
     *
     * @param subscribeIdentity SubscribeIdentity
     * @param throwable         error
     * @return Returns the number of affected rows
     */
    int markFailed(SubscribeIdentity subscribeIdentity, Throwable throwable) throws ConcurrentVersionConflictException;

    /***
     *
     * @param limit
     * @param before {@link java.util.concurrent.TimeUnit#MILLISECONDS}
     * @param maxVersion
     * @return failed event
     */
    List<SubscribeEventEntity> queryFailed(int limit, long before, int maxVersion);

    int compensate(SubscribeEventCompensateEntity subscribeEventCompensationEntity);


}
