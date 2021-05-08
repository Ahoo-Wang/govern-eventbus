package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.zookeeper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.ahoo.eventbus.spring.boot.autoconfigure.EnabledSuffix;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = ZookeeperCompensateProperties.PREFIX + EnabledSuffix.SUFFIX, havingValue = "true")
public @interface ConditionalOnZookeeperCompensateEnabled {

}
