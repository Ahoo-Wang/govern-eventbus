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

package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import java.time.Duration;
import java.util.List;

/**
 * 订阅事件仓储
 *
 * @author ahoo wang
 */
public interface SubscribeEventRepository extends EventRepository {

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
     * @param maxVersion
     * @param before
     * @param range
     * @return failed event
     */
    List<SubscribeEventEntity> queryFailed(int limit, int maxVersion, Duration before, Duration range);

    int compensate(SubscribeEventCompensateEntity subscribeEventCompensationEntity);


}
