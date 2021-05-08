package me.ahoo.eventbus.kafka;

import lombok.var;
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
 * @author ahoo wang
 * create time 2020/5/14 21:44
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
        var consistencySubscriber = subscriberFactory.create(subscriber);
        registerListener(consistencySubscriber);
        registerSubscriber(consistencySubscriber);
    }

    private void registerListener(Subscriber subscriber) {
        MethodKafkaListenerAdapter methodKafkaListenerAdapter = new MethodKafkaListenerAdapter(kafkaEventCodec, subscriber);
        var endpoint = new MethodKafkaListenerEndpoint<Long, PublishEvent>();
        endpoint.setBean(methodKafkaListenerAdapter);
        endpoint.setMethod(MethodKafkaListenerAdapter.getInvokeMethod());
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
