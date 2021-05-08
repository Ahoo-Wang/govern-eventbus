package me.ahoo.eventbus.core.subscriber;

import me.ahoo.eventbus.core.publisher.PublishEvent;

import java.lang.reflect.Method;

/**
 * @author ahoo wang
 */
public interface Subscriber {

    String getName();

    Object invoke(PublishEvent publishEvent);

    Object getTarget();

    Method getMethod();

    String getSubscribeEventName();

    Class<?> getSubscribeEventClass();

    boolean isPublish();
}
