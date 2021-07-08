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

package me.ahoo.eventbus.core.subscriber.impl;

import lombok.Builder;
import lombok.SneakyThrows;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ahoo wang
 */
@Builder
public class DefaultSubscriber implements Subscriber {
    private String name;
    private Object target;
    private Method method;
    private String subscribeEventName;
    private Class<?> subscribeEventClass;
    private boolean publish;

    @SneakyThrows
    @Override
    public Object invoke(PublishEvent publishEvent) {
        try {
            return this.method.invoke(this.target, publishEvent.getEventData());
        } catch (InvocationTargetException invocationTargetEx) {
            throw invocationTargetEx.getTargetException();
        }
    }

    public String getName() {
        return name;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public String getSubscribeEventName() {
        return subscribeEventName;
    }

    public Class<?> getSubscribeEventClass() {
        return subscribeEventClass;
    }

    public boolean isPublish() {
        return publish;
    }
}
