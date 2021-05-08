package me.ahoo.eventbus.core.subscriber.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.core.subscriber.SubscriberNameGenerator;

import java.lang.reflect.Method;

/**
 * @author ahoo wang
 */
@Slf4j
public class SimpleSubscriberNameGenerator implements SubscriberNameGenerator {

    private final String prefix;

    public SimpleSubscriberNameGenerator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String generate(Method subscriberMethod) {
        var subscribeAnnotation = subscriberMethod.getAnnotation(Subscribe.class);
        var subscribeName = subscribeAnnotation.value();
        if (Strings.isNullOrEmpty(subscribeName)) {
            subscribeName = subscriberMethod.getName();
            if (log.isWarnEnabled()) {
                log.warn("generate - method:[{}] -> subscribeName is empty,will use method name:[{}] as subscribeName!", subscriberMethod, subscribeName);
            }
        }
        return prefix + subscribeName;
    }
}
