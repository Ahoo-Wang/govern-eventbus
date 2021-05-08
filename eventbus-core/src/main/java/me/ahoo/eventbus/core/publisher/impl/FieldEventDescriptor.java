package me.ahoo.eventbus.core.publisher.impl;

import lombok.SneakyThrows;
import me.ahoo.eventbus.core.publisher.EventDescriptor;

import java.lang.reflect.Field;

/**
 * @author ahoo wang
 */
public class FieldEventDescriptor implements EventDescriptor {

    private final Field field;
    private final Class<?> eventClass;
    private final String eventName;

    public FieldEventDescriptor(String eventName, Field field) {
        field.setAccessible(true);
        this.field = field;
        this.eventClass = field.getType();
        this.eventName = eventName;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public Class<?> getEventClass() {
        return eventClass;
    }

    @SneakyThrows
    @Override
    public Object getEventData(Object targetObject) {
        return field.get(targetObject);
    }
}
