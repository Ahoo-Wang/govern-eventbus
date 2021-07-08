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
