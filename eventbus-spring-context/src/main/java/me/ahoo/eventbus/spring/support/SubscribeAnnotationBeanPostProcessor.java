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

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.core.subscriber.SubscriberScanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : ahoo wang
 */
@Slf4j
public class SubscribeAnnotationBeanPostProcessor implements BeanPostProcessor, SmartInstantiationAwareBeanPostProcessor, Ordered {

    public static final String BEAN_NAME = SubscribeAnnotationBeanPostProcessor.class.getName();
    private final SubscriberScanner subscriberParser;
    private final SubscriberRegistry subscriberRegistry;
    private ConcurrentHashMap<String, Object> registeredBeans;

    public SubscribeAnnotationBeanPostProcessor(SubscriberScanner subscriberParser,
                                                SubscriberRegistry subscriberRegistry) {
        this.subscriberParser = subscriberParser;
        this.subscriberRegistry = subscriberRegistry;
        this.registeredBeans = new ConcurrentHashMap<>();
    }

    /**
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        register(bean, beanName);
        return bean;
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        register(bean, beanName);
        return bean;
    }

    private void register(Object bean, String beanName) {
        var list = subscriberParser.scan(bean);
        if (list.isEmpty()) {
            return;
        }

        registeredBeans.computeIfAbsent(beanName, name -> {
            list.forEach(subscriberRegistry::subscribe);
            return bean;
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
