package me.ahoo.eventbus.core.subscriber;

/**
 * @author ahoo wang
 */
public interface SubscriberRegistry {
    void subscribe(Subscriber subscriber);

    Subscriber getSubscriber(String subscriberName);

}
