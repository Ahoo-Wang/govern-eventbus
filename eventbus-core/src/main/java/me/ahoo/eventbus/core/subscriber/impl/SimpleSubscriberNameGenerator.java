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

import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.core.subscriber.SubscriberNameGenerator;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * SimpleSubscriberNameGenerator.
 *
 * @author ahoo wang
 */
@Slf4j
public class SimpleSubscriberNameGenerator implements SubscriberNameGenerator {
    
    private final String prefix;
    
    public SimpleSubscriberNameGenerator(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public String generate(Method subscriberMethod) {
        Subscribe subscribeAnnotation = subscriberMethod.getAnnotation(Subscribe.class);
        String subscribeName = subscribeAnnotation.value();
        if (Strings.isNullOrEmpty(subscribeName)) {
            subscribeName = subscriberMethod.getName();
            if (log.isWarnEnabled()) {
                log.warn("generate - method:[{}] -> subscribeName is empty,will use method name:[{}] as subscribeName!", subscriberMethod, subscribeName);
            }
        }
        return prefix + subscribeName;
    }
}
