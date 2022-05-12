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

import me.ahoo.eventbus.core.EventBusException;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SimpleSubscriber.
 *
 * @author ahoo wang
 */
public class SimpleSubscriber implements Subscriber {
    private final String name;
    private final Object target;
    private final Method method;
    private final String subscribeEventName;
    private final Class<?> subscribeEventClass;
    private final boolean rePublish;
    
    public SimpleSubscriber(String name,
                            Object target,
                            Method method,
                            String subscribeEventName,
                            Class<?> subscribeEventClass,
                            boolean rePublish) {
        this.name = name;
        this.target = target;
        this.method = method;
        this.subscribeEventName = subscribeEventName;
        this.subscribeEventClass = subscribeEventClass;
        this.rePublish = rePublish;
    }
    
    @Override
    public Object invoke(PublishEvent publishEvent) {
        try {
            return this.method.invoke(this.target, publishEvent.getEventData());
        } catch (IllegalAccessException illegalAccessException) {
            throw new EventBusException(illegalAccessException.getMessage(), illegalAccessException);
        } catch (InvocationTargetException invocationTargetException) {
            throw new EventBusException(invocationTargetException.getTargetException());
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
    
    public boolean rePublish() {
        return rePublish;
    }
}
