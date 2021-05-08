package me.ahoo.eventbus.spring.annotation;

import me.ahoo.eventbus.spring.support.EventBusConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : ahoo wang
 * @see EventBusConfigurationSelector
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(EventBusConfigurationSelector.class)
public @interface EnableEventBus {
}
