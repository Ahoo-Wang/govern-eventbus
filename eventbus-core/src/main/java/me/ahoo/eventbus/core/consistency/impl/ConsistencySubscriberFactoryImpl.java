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

package me.ahoo.eventbus.core.consistency.impl;

import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriber;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author ahoo wang
 */
public class ConsistencySubscriberFactoryImpl implements ConsistencySubscriberFactory {

    private final ConsistencyPublisher consistencyPublisher;

    private final EventDescriptorParser eventDescriptorParser;
    private final PublishEventRepository publishEventRepository;
    private final SubscribeEventRepository subscribeEventRepository;
    private final PlatformTransactionManager transactionManager;

    public ConsistencySubscriberFactoryImpl(ConsistencyPublisher consistencyPublisher,
                                            EventDescriptorParser eventDescriptorParser,
                                            PublishEventRepository publishEventRepository,
                                            SubscribeEventRepository subscribeEventRepository,
                                            PlatformTransactionManager transactionManager) {
        this.consistencyPublisher = consistencyPublisher;
        this.eventDescriptorParser = eventDescriptorParser;
        this.publishEventRepository = publishEventRepository;
        this.subscribeEventRepository = subscribeEventRepository;

        this.transactionManager = transactionManager;
    }

    @Override
    public ConsistencySubscriber create(Subscriber subscriber) {
        return new ConsistencySubscriberImpl(subscriber, eventDescriptorParser, consistencyPublisher, publishEventRepository, subscribeEventRepository, transactionManager);
    }
}
