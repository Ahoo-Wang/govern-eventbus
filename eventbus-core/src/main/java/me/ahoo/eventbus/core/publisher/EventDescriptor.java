package me.ahoo.eventbus.core.publisher;

/**
 * @author ahoo wang
 */
public interface EventDescriptor {

    String getEventName();

    Class<?> getEventClass();

    Object getEventData(Object targetObject);

}
