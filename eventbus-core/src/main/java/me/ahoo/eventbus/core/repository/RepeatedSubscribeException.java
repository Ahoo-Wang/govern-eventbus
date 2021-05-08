package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.EventBusException;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

/**
 * 重复订阅事件异常
 *
 * @author ahoo wang
 */
public class RepeatedSubscribeException extends EventBusException {
    private final Subscriber subscriber;
    private final PublishEvent publishEvent;
    private final String errorMsg;

    public RepeatedSubscribeException(Subscriber subscriber, PublishEvent publishEvent) {
        this.subscriber = subscriber;
        this.publishEvent = publishEvent;
        errorMsg = String.format("Subscriber.name:[%s] -> id:[%d]", subscriber.getName(), publishEvent.getId());
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public PublishEvent getPublishEvent() {
        return publishEvent;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
