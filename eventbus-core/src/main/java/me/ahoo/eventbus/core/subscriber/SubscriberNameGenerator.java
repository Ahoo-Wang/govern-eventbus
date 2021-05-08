package me.ahoo.eventbus.core.subscriber;


import java.lang.reflect.Method;

/**
 * @author ahoo wang
 */
public interface SubscriberNameGenerator {

    String generate(Method subscriberMethod);

}
