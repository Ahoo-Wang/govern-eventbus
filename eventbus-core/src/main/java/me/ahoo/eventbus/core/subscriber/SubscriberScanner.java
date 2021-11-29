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

package me.ahoo.eventbus.core.subscriber;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.core.publisher.EventDescriptor;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.subscriber.impl.SimpleSubscriber;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ahoo wang
 */
@Slf4j
public class SubscriberScanner {

    private final SubscriberNameGenerator subscriberNameGenerator;
    private final EventDescriptorParser eventDescriptorParser;
    public SubscriberScanner(SubscriberNameGenerator subscriberNameGenerator, EventDescriptorParser eventDescriptorParser) {
        this.subscriberNameGenerator = subscriberNameGenerator;
        this.eventDescriptorParser = eventDescriptorParser;
    }

    private SimpleSubscriber parseSubscriber(Object subscribeTarget, Method method, Subscribe subscribeAnnotation) {
        Preconditions.checkState(method.getParameterCount() == 1, "method:[%s] ParameterCount must be 1.", method);
        String subscribeName = subscriberNameGenerator.generate(method);
        Class<?> subscribeParameterType = method.getParameterTypes()[0];
        EventDescriptor subscribeEventDescriptor = eventDescriptorParser.get(subscribeParameterType);
        boolean rePublish = subscribeAnnotation.rePublish();
        Class<?> returnType = method.getReturnType();
        Event returnTypeAnnotation = returnType.getAnnotation(Event.class);
        if (Objects.nonNull(returnTypeAnnotation)) {
            rePublish = true;
        }
        return new SimpleSubscriber(subscribeName, subscribeTarget, method, subscribeEventDescriptor.getEventName(), subscribeEventDescriptor.getEventClass(), rePublish);
    }

    public List<Subscriber> scan(Object subscribeTarget) {

        Class<?> subscribeClass = AopUtils.getTargetClass(subscribeTarget);
        List<Subscriber> subscribers = new ArrayList<Subscriber>();

        ReflectionUtils.doWithMethods(subscribeClass, method -> {
            Subscribe subscribeAnnotation = AnnotationUtils.findAnnotation(method, Subscribe.class);
            if (Objects.isNull(subscribeAnnotation)) {
                return;
            }
            SimpleSubscriber subscriber = parseSubscriber(subscribeTarget, method, subscribeAnnotation);
            subscribers.add(subscriber);
        }, ReflectionUtils.USER_DECLARED_METHODS);

        return subscribers;
    }
}
