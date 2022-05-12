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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * EventBusBootstrapConfiguration.
 *
 * @author : ahoo wang
 */
public class EventBusBootstrapConfiguration implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        
        if (!registry.containsBeanDefinition(PublishAnnotationAspect.BEAN_NAME)) {
            registry.registerBeanDefinition(PublishAnnotationAspect.BEAN_NAME,
                new RootBeanDefinition(PublishAnnotationAspect.class));
        }
        
        if (!registry.containsBeanDefinition(SubscriberLifecycle.BEAN_NAME)) {
            registry.registerBeanDefinition(SubscriberLifecycle.BEAN_NAME,
                new RootBeanDefinition(SubscriberLifecycle.class));
        }
        
    }
}
