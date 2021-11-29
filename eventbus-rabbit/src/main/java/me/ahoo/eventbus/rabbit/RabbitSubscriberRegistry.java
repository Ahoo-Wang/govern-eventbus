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

package me.ahoo.eventbus.rabbit;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.rabbit.config.RabbitConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author ahoo wang
 */
@Slf4j
public class RabbitSubscriberRegistry implements SubscriberRegistry {
    private RabbitEventCodec rabbitEventCodec;
    private final ConsistencySubscriberFactory subscriberFactory;
    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
    private final DirectRabbitListenerContainerFactory listenerContainerFactory;
    private final RabbitAdmin rabbitAdmin;
    private final Exchange exchange;
    private final ConcurrentHashMap<String, Subscriber> subscriberNameMap;


    public RabbitSubscriberRegistry(RabbitEventCodec rabbitEventCodec, RabbitConfig rabbitConfig,
                                    ConnectionFactory connectionFactory,
                                    ConsistencySubscriberFactory subscriberFactory,
                                    RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry) {
        this.rabbitEventCodec = rabbitEventCodec;
        this.exchange = ExchangeBuilder.directExchange(rabbitConfig.getExchange()).build();
        this.subscriberFactory = subscriberFactory;
        this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
        this.listenerContainerFactory = new DirectRabbitListenerContainerFactory();
        this.listenerContainerFactory.setConnectionFactory(connectionFactory);
        this.listenerContainerFactory.setContainerCustomizer(container -> {
            String listenerId = container.getListenerId();
            container.setBeanName(listenerId);
        });
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.subscriberNameMap = new ConcurrentHashMap<>();
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        initSubscribeQueue(subscriber);
        var consistencySubscriber = subscriberFactory.create(subscriber);
        registerListener(consistencySubscriber);
        registerSubscriber(consistencySubscriber);
    }

    private void initSubscribeQueue(Subscriber subscriber) {
        var queueName = subscriber.getName();
        var subscriberQueue = QueueBuilder.durable(queueName).build();
        rabbitAdmin.declareQueue(subscriberQueue);

        /**
         * Bind Self
         */
        if (log.isInfoEnabled()) {
            log.info("initSubscribeQueue - Bind Self {}-> QueueName:[{}],RouterKey:[{}]", exchange, queueName, queueName);
        }
        Binding selfBinding = BindingBuilder.bind(subscriberQueue).to(exchange).with(queueName).noargs();
        rabbitAdmin.declareBinding(selfBinding);
        if (log.isInfoEnabled()) {
            log.info("initSubscribeQueue - Bind {}-> QueueName:[{}],RouterKey:[{}]", exchange, queueName, subscriber.getSubscribeEventName());
        }
        Binding routingKeyBinding = BindingBuilder.bind(subscriberQueue).to(exchange).with(subscriber.getSubscribeEventName()).noargs();
        rabbitAdmin.declareBinding(routingKeyBinding);
    }

    private void registerListener(Subscriber subscriber) {
        var eventListener = new RabbitEventListener(rabbitEventCodec, subscriber);
        var endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(subscriber.getName());
        endpoint.setQueueNames(subscriber.getName());
        endpoint.setMessageListener(eventListener);
        rabbitListenerEndpointRegistry.registerListenerContainer(endpoint, listenerContainerFactory, true);
    }

    private void registerSubscriber(Subscriber subscriber) {
        subscriberNameMap.put(subscriber.getName(), subscriber);
    }

    @Override
    public Subscriber getSubscriber(String subscriberName) {
        return subscriberNameMap.get(subscriberName);
    }


}
