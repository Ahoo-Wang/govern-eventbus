package me.ahoo.eventbus.core.publisher.impl;

import me.ahoo.eventbus.core.publisher.EventDescriptor;

/**
 * @author ahoo wang
 */
public class SimpleEventDescriptor implements EventDescriptor {
    private final Class<?> eventClass;
    private final String eventName;

    public SimpleEventDescriptor(String eventName, Class<?> eventClass) {
        this.eventClass = eventClass;
        this.eventName = eventName;
    }

    @Override
    public Class<?> getEventClass() {
        return eventClass;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public Object getEventData(Object targetObject) {
        return targetObject;
    }
}
