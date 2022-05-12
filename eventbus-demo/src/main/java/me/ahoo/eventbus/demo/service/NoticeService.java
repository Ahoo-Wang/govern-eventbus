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

import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * NoticeService.
 *
 * @author ahoo wang
 */
@Slf4j
@Service
public class NoticeService {
    
    @Subscribe
    public void handleOrderCreated(OrderCreatedEvent orderCreatedEvent) {
        log.info("handleOrderCreated - event:[{}].", orderCreatedEvent);
        /**
         * Execute business code
         * send sms / email ?
         */
    }
}
