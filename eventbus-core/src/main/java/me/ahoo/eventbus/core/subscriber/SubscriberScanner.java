package me.ahoo.eventbus.core.subscriber;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.subscriber.impl.DefaultSubscriber;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ahoo wang
 */
@Slf4j
public class SubscriberScanner {

    private final SubscriberNameGenerator subscriberNameGenerator;
    private final EventDescriptorParser eventDescriptorParser;

    public SubscriberScanner(SubscriberNameGenerator subscriberNameGenerator, EventDescriptorParser eventDescriptorParser) {
        this.subscriberNameGenerator = subscriberNameGenerator;

        this.eventDescriptorParser = eventDescriptorParser;
    }

    public List<Subscriber> scan(Object subscribeTarget) {

        var subscribeClass = AopUtils.getTargetClass(subscribeTarget);
        var subscribers = new ArrayList<Subscriber>();

        ReflectionUtils.doWithMethods(subscribeClass, method -> {
            var subscribeAnnotation = AnnotationUtils.findAnnotation(method, Subscribe.class);
            if (Objects.isNull(subscribeAnnotation)) {
                return;
            }
            DefaultSubscriber subscriber = parseSubscriber(subscribeTarget, method, subscribeAnnotation);
            subscribers.add(subscriber);
        }, ReflectionUtils.USER_DECLARED_METHODS);

        return subscribers;
    }

    private DefaultSubscriber parseSubscriber(Object subscribeTarget, Method method, Subscribe subscribeAnnotation) {
        Preconditions.checkState(method.getParameterCount() == 1, "method:[%s] ParameterCount must be 1.", method);
        var subscribeName = subscriberNameGenerator.generate(method);
        var subscribeParameterType = method.getParameterTypes()[0];
        var subscribeEventDescriptor = eventDescriptorParser.parse(subscribeParameterType);
        var isPublish = subscribeAnnotation.isPublish();
        var returnType = method.getReturnType();
        var returnTypeAnnotation = returnType.getAnnotation(Event.class);
        if (Objects.nonNull(returnTypeAnnotation)) {
            isPublish = true;
        }

        return DefaultSubscriber.builder()
                .target(subscribeTarget)
                .method(method)
                .name(subscribeName)
                .subscribeEventClass(subscribeEventDescriptor.getEventClass())
                .subscribeEventName(subscribeEventDescriptor.getEventName())
                .publish(isPublish)
                .build();
    }

}
