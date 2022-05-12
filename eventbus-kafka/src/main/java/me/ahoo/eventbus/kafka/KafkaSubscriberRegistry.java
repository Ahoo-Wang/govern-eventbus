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

package me.ahoo.eventbus.kafka;

import me.ahoo.eventbus.core.consistency.ConsistencySubscriber;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;

import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * KafkaSubscriberRegistry.
 *
 * @author ahoo wang
 */
public class KafkaSubscriberRegistry implements SubscriberRegistry {
    
    private final KafkaEventCodec kafkaEventCodec;
    private final ConcurrentHashMap<String, Subscriber> nameMapSubscribers;
    private final MessageHandlerMethodFactory messageHandlerMethodFactory;
    private final ConsistencySubscriberFactory subscriberFactory;
    private final KafkaListenerEndpointRegistry listenerEndpointRegistry;
    private final KafkaListenerContainerFactory listenerContainerFactory;
    
    public KafkaSubscriberRegistry(KafkaEventCodec kafkaEventCodec, ConsistencySubscriberFactory subscriberFactory,
                                   KafkaListenerEndpointRegistry listenerEndpointRegistry,
                                   KafkaListenerContainerFactory listenerContainerFactory) {
        this.kafkaEventCodec = kafkaEventCodec;
        this.subscriberFactory = subscriberFactory;
        this.listenerEndpointRegistry = listenerEndpointRegistry;
        this.listenerContainerFactory = listenerContainerFactory;
        this.nameMapSubscribers = new ConcurrentHashMap<>();
        this.messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
    }
    
    @Override
    public void subscribe(Subscriber subscriber) {
        ConsistencySubscriber consistencySubscriber = subscriberFactory.create(subscriber);
        registerListener(consistencySubscriber);
        registerSubscriber(consistencySubscriber);
    }
    
    private void registerListener(Subscriber subscriber) {
        MethodKafkaListenerAdapter methodKafkaListenerAdapter = new MethodKafkaListenerAdapter(kafkaEventCodec, subscriber);
        MethodKafkaListenerEndpoint endpoint = new MethodKafkaListenerEndpoint<Long, PublishEvent>();
        endpoint.setBean(methodKafkaListenerAdapter);
        endpoint.setMethod(MethodKafkaListenerAdapter.INVOKE_METHOD);
        endpoint.setId(subscriber.getName());
        endpoint.setGroupId(subscriber.getName());
        endpoint.setTopics(subscriber.getSubscribeEventName());
        endpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
        this.listenerEndpointRegistry.registerListenerContainer(endpoint, this.listenerContainerFactory, true);
    }
    
    private void registerSubscriber(Subscriber subscriber) {
        nameMapSubscribers.put(subscriber.getName(), subscriber);
    }
    
    @Override
    public Subscriber getSubscriber(String subscriberName) {
        return nameMapSubscribers.get(subscriberName);
    }
}
