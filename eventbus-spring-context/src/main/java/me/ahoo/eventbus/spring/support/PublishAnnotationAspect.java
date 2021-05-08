package me.ahoo.eventbus.spring.support;

import me.ahoo.eventbus.core.EventBusException;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author : ahoo wang
 */
@Aspect
public class PublishAnnotationAspect {

    public static final String BEAN_NAME = PublishAnnotationAspect.class.getName();
    private final ConsistencyPublisher consistencyPublisher;

    public PublishAnnotationAspect(ConsistencyPublisher consistencyPublisher) {
        this.consistencyPublisher = consistencyPublisher;
    }

    @Pointcut(value = "@annotation(me.ahoo.eventbus.core.annotation.Publish)")
    public void publish() {

    }


    @Around("publish()")
    public Object publishConsistency(ProceedingJoinPoint proceedingJoinPoint) {
        return consistencyPublisher.publish(() -> {
            try {
                return proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new EventBusException(throwable);
            }
        });
    }

}
