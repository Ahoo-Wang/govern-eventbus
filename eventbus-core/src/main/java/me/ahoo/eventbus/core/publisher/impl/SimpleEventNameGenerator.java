package me.ahoo.eventbus.core.publisher.impl;

import com.google.common.base.Strings;
import lombok.var;
import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.publisher.EventNameGenerator;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahoo wang
 */
public class SimpleEventNameGenerator implements EventNameGenerator {
    private final ConcurrentHashMap<Class<?>, String> eventClazzMap = new ConcurrentHashMap<>();

    @Override
    public String generate(Class<?> eventClass) {
        return eventClazzMap.computeIfAbsent(eventClass, eClazz -> {
            var eventClassAnnotation = eClazz.getAnnotation(Event.class);
            if (Objects.nonNull(eventClassAnnotation) && !Strings.isNullOrEmpty(eventClassAnnotation.value())) {
                return eventClassAnnotation.value();
            }
            return eClazz.getSimpleName();
        });
    }
}
