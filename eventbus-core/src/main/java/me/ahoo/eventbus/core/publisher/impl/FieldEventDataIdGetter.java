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

import me.ahoo.eventbus.core.publisher.EventDataIdGetter;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * FieldEventDataIdGetter.
 *
 * @author ahoo wang
 */
public class FieldEventDataIdGetter implements EventDataIdGetter {
    
    private final Field eventDataIdField;
    
    public FieldEventDataIdGetter(Field eventDataIdField) {
        this.eventDataIdField = eventDataIdField;
        eventDataIdField.setAccessible(true);
    }
    
    @Override
    public long getEventDataId(Object targetObject) {
        try {
            Object eventDataId = eventDataIdField.get(targetObject);
            if (Objects.isNull(eventDataId)) {
                return DEFAULT_EVENT_DATA_ID;
            }
            return (long) eventDataId;
        } catch (IllegalAccessException e) {
            return DEFAULT_EVENT_DATA_ID;
        }
    }
}
