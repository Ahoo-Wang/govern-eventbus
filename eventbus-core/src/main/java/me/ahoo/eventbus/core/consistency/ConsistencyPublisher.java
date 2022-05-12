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

package me.ahoo.eventbus.core.consistency;

import me.ahoo.eventbus.core.repository.PublishIdentity;

import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * execution flow.
 * <pre>
 * 1. begin local transaction
 * 2. invoke local biz code
 * 3. insert publish event:INITIALIZED to local db
 * 4. commit local transaction
 * --- try
 * 5. publish event to event-bus(MQ)
 * 6. update publish event status to SUCCEEDED
 * --- catch update publish event to FAILED
 * </pre>
 *
 * @author ahoo wang
 */
public interface ConsistencyPublisher {
    
    Object publish(Supplier<Object> publishDataSupplier);
    
    /**
     * publish event to bus.
     *
     * @param publishIdentity publish event id
     * @param publishEventData publish event data
     * @return the Future of publish
     */
    Future<?> publish(PublishIdentity publishIdentity, Object publishEventData);
}
