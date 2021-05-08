package me.ahoo.eventbus.spring.support;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author : ahoo wang
 */
public class EventBusBootstrapConfiguration implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        if (!registry.containsBeanDefinition(PublishAnnotationAspect.BEAN_NAME)) {
            registry.registerBeanDefinition(PublishAnnotationAspect.BEAN_NAME,
                    new RootBeanDefinition(PublishAnnotationAspect.class));
        }

        if (!registry.containsBeanDefinition(SubscribeAnnotationBeanPostProcessor.BEAN_NAME)) {
            registry.registerBeanDefinition(SubscribeAnnotationBeanPostProcessor.BEAN_NAME,
                    new RootBeanDefinition(SubscribeAnnotationBeanPostProcessor.class));
        }

    }
}
