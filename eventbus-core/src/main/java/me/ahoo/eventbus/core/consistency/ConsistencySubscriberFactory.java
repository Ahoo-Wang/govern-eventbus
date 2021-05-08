package me.ahoo.eventbus.core.consistency;

import me.ahoo.eventbus.core.subscriber.Subscriber;

/**
 * @author ahoo wang
 */
public interface ConsistencySubscriberFactory {
    ConsistencySubscriber create(Subscriber subscriber);
}
