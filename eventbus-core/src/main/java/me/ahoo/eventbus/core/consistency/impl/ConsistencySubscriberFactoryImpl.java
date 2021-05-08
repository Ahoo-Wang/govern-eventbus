package me.ahoo.eventbus.core.consistency.impl;

import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriber;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
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
