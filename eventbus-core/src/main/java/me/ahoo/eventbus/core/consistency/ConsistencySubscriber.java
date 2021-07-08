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

import me.ahoo.eventbus.core.subscriber.Subscriber;

/**
 * execution flow
 * getSubscribeEventBy event_id and event_name and subscribe_name
 * if exist
 * status is SUCCEEDED throw RepeatedSubscribeException
 * status is INITIALIZED
 * else insert subscribe event:INITIALIZED to local db
 * ---- try
 * 1. begin local transaction
 * 2. update subscribe event to SUCCEEDED (Optimistic lock update)
 * 3. invoke local biz code
 * 4. commit local transaction
 * ---- catch update subscribe event to FAILED
 *
 * @author ahoo wang
 */
public interface ConsistencySubscriber extends Subscriber {

    Subscriber getTargetSubscriber();

}
