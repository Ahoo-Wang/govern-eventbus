package me.ahoo.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * 字段事件
 *
 * @author ahoo wang
 */
@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldEvent {
}
