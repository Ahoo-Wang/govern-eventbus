package me.ahoo.eventbus.core.subscriber.impl;

import lombok.*;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ahoo wang
 */
@Builder
public class DefaultSubscriber implements Subscriber {
    private String name;
    private Object target;
    private Method method;
    private String subscribeEventName;
    private Class<?> subscribeEventClass;
    private boolean publish;

    @SneakyThrows
    @Override
    public Object invoke(PublishEvent publishEvent) {
        try {
            return this.method.invoke(this.target, publishEvent.getEventData());
        } catch (InvocationTargetException invocationTargetEx) {
            throw invocationTargetEx.getTargetException();
        }
    }

    public String getName() {
        return name;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public String getSubscribeEventName() {
        return subscribeEventName;
    }

    public Class<?> getSubscribeEventClass() {
        return subscribeEventClass;
    }

    public boolean isPublish() {
        return publish;
    }
}
