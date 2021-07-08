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
