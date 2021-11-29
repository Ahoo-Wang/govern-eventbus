/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.eventbus.core.publisher;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.publisher.impl.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author ahoo wang
 */
@Slf4j
public class EventDescriptorParser {

    private final ConcurrentHashMap<Class<?>, EventDescriptor> eventDescriptorMap = new ConcurrentHashMap<>();
    private final EventNameGenerator eventNameGenerator;

    public EventDescriptorParser(EventNameGenerator eventNameGenerator) {
        this.eventNameGenerator = eventNameGenerator;
    }

    public EventDescriptor get(final Object targetObject) {
        return get(targetObject.getClass());
    }

    public EventDescriptor get(final Class<?> targetClass) {
        return eventDescriptorMap.computeIfAbsent(targetClass, eClazz -> parse(targetClass));
    }

    public EventDescriptor parse(final Class<?> targetClass) {

        Event eventClassAnnotation = targetClass.getAnnotation(Event.class);

        if (Objects.nonNull(eventClassAnnotation)) {
            Class<?> eventClass = targetClass;
            EventDataIdGetter eventDataIdGetter = getEventDataIdGetter(eventClass, eventClassAnnotation.dataId());
            String eventName = getEventName(eventClassAnnotation, eventClass);
            return new SimpleEventDescriptor(eventName, eventClass, new SimpleEventDataGetter(), eventDataIdGetter);
        }
        /**
         * event data field
         */
        Optional<Field> eventFieldOp = filterField(targetClass, (declaredField) -> declaredField.isAnnotationPresent(Event.class));

        if (eventFieldOp.isPresent()) {
            Field eventDataField = eventFieldOp.get();
            eventClassAnnotation = eventDataField.getAnnotation(Event.class);
            Class<?> eventClass = eventDataField.getType();
            EventDataIdGetter eventDataIdGetter = getEventDataIdGetter(eventClass, eventClassAnnotation.dataId());
            String eventName = getEventName(eventClassAnnotation, eventClass);
            return new SimpleEventDescriptor(eventName, eventClass, new FieldEventDataGetter(eventDataField), eventDataIdGetter);
        }

        String eventName = getEventName(null, targetClass);
        EventDataIdGetter eventDataIdGetter = getEventDataIdGetter(targetClass, EventDataIdGetter.DEFAULT_ID_FIELD_NAME);
        return new SimpleEventDescriptor(eventName, targetClass, new SimpleEventDataGetter(), eventDataIdGetter);
    }

    private EventDataIdGetter getEventDataIdGetter(Class<?> eventClass, String dataIdFieldName) {
        Optional<Field> eventDataIdFieldOp = filterField(eventClass, (declaredField ->
                EventDataIdGetter.availableType(declaredField.getType())
                        && dataIdFieldName.equals(declaredField.getName())
        ));
        if (eventDataIdFieldOp.isPresent()) {
            return new FieldEventDataIdGetter(eventDataIdFieldOp.get());
        }
        return NoneEventDataIdGetter.INSTANCE;
    }

    private String getEventName(@Nullable Event eventAnnotation, Class<?> eventClass) {
        return eventNameGenerator.generate(eventAnnotation, eventClass);
    }

    public static Optional<Field> filterField(Class<?> targetClass, Predicate<Field> filter) {
        Class<?> currentDeclaringClass = targetClass;
        while (!Object.class.equals(currentDeclaringClass)) {
            for (Field declaredField : currentDeclaringClass.getDeclaredFields()) {
                if (filter.test(declaredField)) {
                    return Optional.of(declaredField);
                }
            }
            currentDeclaringClass = currentDeclaringClass.getSuperclass();
        }
        return Optional.empty();
    }
}
