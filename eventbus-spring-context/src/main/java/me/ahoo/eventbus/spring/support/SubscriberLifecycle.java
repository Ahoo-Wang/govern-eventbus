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

package me.ahoo.eventbus.spring.support;

import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.core.subscriber.SubscriberScanner;

import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SubscriberLifecycle.
 *
 * @author ahoo wang
 */
public class SubscriberLifecycle implements SmartLifecycle {
    
    public static final String BEAN_NAME = SubscriberLifecycle.class.getName();
    private final ApplicationContext context;
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, Object> registeredBeans;
    
    public SubscriberLifecycle(ApplicationContext applicationContext) {
        this.context = applicationContext;
        this.registeredBeans = new ConcurrentHashMap<>();
    }
    
    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        
        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanName);
            register(bean, beanName);
        }
    }
    
    private void register(Object bean, String beanName) {
        final SubscriberScanner subscriberScanner = context.getBean(SubscriberScanner.class);
        List<Subscriber> list = subscriberScanner.scan(bean);
        if (list.isEmpty()) {
            return;
        }
        final SubscriberRegistry subscriberRegistry = context.getBean(SubscriberRegistry.class);
        registeredBeans.computeIfAbsent(beanName, name -> {
            list.forEach(subscriberRegistry::subscribe);
            return bean;
        });
    }
    
    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
}
