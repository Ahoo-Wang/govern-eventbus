/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.eventbus.core.consistency.impl;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriber;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public class ConsistencySubscriberImpl implements ConsistencySubscriber {

    private final Subscriber targetSubscriber;
    private final EventDescriptorParser eventDescriptorParser;
    private final ConsistencyPublisher consistencyPublisher;
    private final PublishEventRepository publishEventRepository;
    private final SubscribeEventRepository subscribeEventRepository;
    private final PlatformTransactionManager transactionManager;

    public ConsistencySubscriberImpl(Subscriber targetSubscriber,
                                     EventDescriptorParser eventDescriptorParser,
                                     ConsistencyPublisher consistencyPublisher,
                                     PublishEventRepository publishEventRepository,
                                     SubscribeEventRepository subscribeEventRepository,
                                     PlatformTransactionManager transactionManager) {
        this.targetSubscriber = targetSubscriber;
        this.eventDescriptorParser = eventDescriptorParser;
        this.consistencyPublisher = consistencyPublisher;
        this.publishEventRepository = publishEventRepository;
        this.subscribeEventRepository = subscribeEventRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public String getName() {
        return targetSubscriber.getName();
    }

    @Override
    public Subscriber getTargetSubscriber() {
        return targetSubscriber;
    }

    /**
     * @param subscribePublishEvent PublishEvent
     */
    @Override
    public Object invoke(PublishEvent subscribePublishEvent) {

        var stopwatch = Stopwatch.createStarted();
        var subscribeIdentity = subscribeEventRepository.initialize(targetSubscriber, subscribePublishEvent);

        PublishIdentity publishIdentity = null;
        Object publishEventData = null;

        var transactionStatus = transactionManager.getTransaction(null);
        try {
            /**
             *  mark first!
             */
            subscribeEventRepository.markSucceeded(subscribeIdentity);
            var returnVal = targetSubscriber.invoke(subscribePublishEvent);
            if (targetSubscriber.isPublish() && Objects.nonNull(returnVal)) {
                var eventDescriptor = eventDescriptorParser.parse(returnVal);
                publishEventData = eventDescriptor.getEventData(returnVal);
                if (Objects.nonNull(publishEventData)) {
                    publishIdentity = publishEventRepository.initialize(eventDescriptor.getEventName(), publishEventData);
                }
            }
        } catch (Throwable throwable) {
            transactionManager.rollback(transactionStatus);
            try {
                subscribeEventRepository.markFailed(subscribeIdentity, throwable);
            } catch (Throwable subscribeFailedThrowable) {
                var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                var markFailedError = String.format("invoke - mark subscribe event status to failed error. -> id:[%d] error,taken:[%d]ms!"
                        , subscribeIdentity.getId()
                        , taken);
                log.error(markFailedError, subscribeFailedThrowable);
            }
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            if (log.isInfoEnabled()) {
                log.info("invoke - Subscribe failed! -> id:[{}],taken:[{}]", subscribeIdentity.getId(), taken);
            }
            throw throwable;
        }
        transactionManager.commit(transactionStatus);
        var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        if (log.isDebugEnabled()) {
            log.debug("invoke - Subscribe succeeded! -> id:[{}],taken:[{}]", subscribeIdentity.getId(), taken);
        }
        if (!targetSubscriber.isPublish()) {
            return publishEventData;
        }
        if (Objects.isNull(publishIdentity)) {
            if (log.isWarnEnabled()) {
                log.warn("invoke - subscribe: [{}] -> Ignore publish event when publishEvent is null.", targetSubscriber.getName());
            }
            return publishEventData;
        }

        consistencyPublisher.publish(publishIdentity, publishEventData);
        return publishEventData;
    }

    @Override
    public Object getTarget() {
        return targetSubscriber.getTarget();
    }

    @Override
    public Method getMethod() {
        return targetSubscriber.getMethod();
    }

    @Override
    public String getSubscribeEventName() {
        return targetSubscriber.getSubscribeEventName();
    }

    @Override
    public Class<?> getSubscribeEventClass() {
        return targetSubscriber.getSubscribeEventClass();
    }

    @Override
    public boolean isPublish() {
        return targetSubscriber.isPublish();
    }
}
