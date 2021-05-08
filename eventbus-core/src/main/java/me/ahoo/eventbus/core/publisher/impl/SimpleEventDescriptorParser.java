package me.ahoo.eventbus.core.publisher.impl;

import lombok.var;
import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.annotation.FieldEvent;
import me.ahoo.eventbus.core.publisher.EventDescriptor;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.EventNameGenerator;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahoo wang
 */
public class SimpleEventDescriptorParser implements EventDescriptorParser {
    private final EventNameGenerator eventNameGenerator;
    private static final ConcurrentHashMap<Class<?>, EventDescriptor> eventClazzMap = new ConcurrentHashMap<>();

    public SimpleEventDescriptorParser(EventNameGenerator eventNameGenerator) {
        this.eventNameGenerator = eventNameGenerator;
    }

    @Override
    public EventDescriptor parse(final Class<?> eventClazz) {
        return eventClazzMap.computeIfAbsent(eventClazz, eClazz -> {

            var eventClassAnnotation = eClazz.getAnnotation(Event.class);
            if (Objects.nonNull(eventClassAnnotation)) {
                var eventName = eventNameGenerator.generate(eClazz);
                return new SimpleEventDescriptor(eventName, eClazz);
            }

            var optionalField = Arrays.stream(eClazz.getDeclaredFields())
                    .filter(field -> Objects.nonNull(field.getAnnotation(FieldEvent.class)))
                    .findFirst();
            if (optionalField.isPresent()) {
                var fieldEvent = optionalField.get();
                var fieldEventClazz = fieldEvent.getType();
                var eventName = eventNameGenerator.generate(fieldEventClazz);
                return new FieldEventDescriptor(eventName, fieldEvent);
            }

            var eventName = eventNameGenerator.generate(eClazz);
            return new SimpleEventDescriptor(eventName, eClazz);
        });
    }
}
