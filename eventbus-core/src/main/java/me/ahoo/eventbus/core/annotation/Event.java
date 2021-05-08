package me.ahoo.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * @author ahoo wang
 */
@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Event {
    /**
     * event name
     * <p>
     * kafka:topic
     * rabbit:routeKey
     *
     * @return event name
     */
    String value() default "";
}
