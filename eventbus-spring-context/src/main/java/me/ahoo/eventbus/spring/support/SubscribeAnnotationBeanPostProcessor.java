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
    private final SubscriberScanner subscriberScanner;
    private final SubscriberRegistry subscriberRegistry;
    private ConcurrentHashMap<String, Object> registeredBeans;

    public SubscribeAnnotationBeanPostProcessor(SubscriberScanner subscriberScanner,
                                                SubscriberRegistry subscriberRegistry) {
        this.subscriberScanner = subscriberScanner;
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
        var list = subscriberScanner.scan(bean);
        if (list.isEmpty()) {
            return;
        }

        registeredBeans.computeIfAbsent(beanName, name -> {
            list.forEach(simpleSubscribeInvocation -> {
                subscriberRegistry.subscribe(simpleSubscribeInvocation);
            });
            return bean;
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
