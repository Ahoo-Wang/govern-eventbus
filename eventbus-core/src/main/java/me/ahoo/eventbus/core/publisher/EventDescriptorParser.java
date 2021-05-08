package me.ahoo.eventbus.core.publisher;

/**
 * @author ahoo wang
 */
public interface EventDescriptorParser {

    EventDescriptor parse(Class<?> eventClazz);

    default EventDescriptor parse(Object eventObject) {
        return parse(eventObject.getClass());
    }
}
