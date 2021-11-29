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

import me.ahoo.eventbus.core.publisher.EventDataGetter;
import me.ahoo.eventbus.core.publisher.EventDataIdGetter;
import me.ahoo.eventbus.core.publisher.EventDescriptor;

/**
 * @author ahoo wang
 */
public class SimpleEventDescriptor implements EventDescriptor {

    private final String eventName;
    private final Class<?> eventClass;
    private final EventDataGetter eventDataGetter;
    private final EventDataIdGetter eventDataIdGetter;

    public SimpleEventDescriptor(String eventName,
                                 Class<?> eventClass,
                                 EventDataGetter eventDataGetter,
                                 EventDataIdGetter eventDataIdGetter) {
        this.eventName = eventName;
        this.eventClass = eventClass;
        this.eventDataGetter = eventDataGetter;
        this.eventDataIdGetter = eventDataIdGetter;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public Class<?> getEventClass() {
        return eventClass;
    }

    @Override
    public Object getEventData(Object targetObject) {
        return eventDataGetter.getEventData(targetObject);
    }

    @Override
    public long getEventDataId(Object targetObject) {
        return eventDataIdGetter.getEventDataId(targetObject);
    }
}
