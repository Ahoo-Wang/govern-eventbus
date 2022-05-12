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

package me.ahoo.eventbus.demo.service;

import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.snowflake.SafeJavaScriptSnowflakeId;
import me.ahoo.eventbus.core.annotation.Publish;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;

import org.springframework.stereotype.Service;

/**
 * OrderService.
 *
 * @author ahoo wang
 */
@Service
public class OrderService {
    private final IdGenerator idGenerator;
    
    public OrderService() {
        idGenerator = SafeJavaScriptSnowflakeId.ofMillisecond(0);
    }
    
    @Publish
    public OrderCreatedEvent createOrder() {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrderId(idGenerator.generate());
        return orderCreatedEvent;
    }
}
