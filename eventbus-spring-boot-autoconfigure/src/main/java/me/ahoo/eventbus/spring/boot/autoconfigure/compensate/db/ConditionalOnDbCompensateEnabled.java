package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.db;

import me.ahoo.eventbus.spring.boot.autoconfigure.EnabledSuffix;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = DbCompensateProperties.PREFIX + EnabledSuffix.SUFFIX, havingValue = "true", matchIfMissing = true)
public @interface ConditionalOnDbCompensateEnabled {

}
