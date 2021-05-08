package me.ahoo.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * @author : ahoo wang
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Subscribe {
    /**
     * subscribe name
     * <p>
     * kafka:consumerId
     * rabbit:queueName
     *
     * @return queue name
     */
    String value() default "";

    boolean isPublish() default false;
}
