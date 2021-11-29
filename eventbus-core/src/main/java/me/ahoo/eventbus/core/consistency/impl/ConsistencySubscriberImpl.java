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
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriber;
import me.ahoo.eventbus.core.publisher.EventDescriptor;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeIdentity;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public class ConsistencySubscriberImpl implements ConsistencySubscriber {

    private final Subscriber targetSubscriber;
    private final ConsistencyPublisher consistencyPublisher;
    private final PublishEventRepository publishEventRepository;
    private final SubscribeEventRepository subscribeEventRepository;
    private final PlatformTransactionManager transactionManager;
    private final EventDescriptorParser eventDescriptorParser;

    public ConsistencySubscriberImpl(Subscriber targetSubscriber,
                                     ConsistencyPublisher consistencyPublisher,
                                     PublishEventRepository publishEventRepository,
                                     SubscribeEventRepository subscribeEventRepository,
                                     PlatformTransactionManager transactionManager, EventDescriptorParser eventDescriptorParser) {
        this.targetSubscriber = targetSubscriber;
        this.consistencyPublisher = consistencyPublisher;
        this.publishEventRepository = publishEventRepository;
        this.subscribeEventRepository = subscribeEventRepository;
        this.transactionManager = transactionManager;
        this.eventDescriptorParser = eventDescriptorParser;
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
        Stopwatch stopwatch = Stopwatch.createStarted();
        SubscribeIdentity subscribeIdentity = subscribeEventRepository.initialize(targetSubscriber, subscribePublishEvent);

        PublishIdentity publishIdentity = null;
        Object publishEventData = null;

        TransactionStatus transactionStatus = transactionManager.getTransaction(null);
        try {
            /**
             *  mark first!
             */
            subscribeEventRepository.markSucceeded(subscribeIdentity);
            Object returnVal = targetSubscriber.invoke(subscribePublishEvent);
            if (targetSubscriber.rePublish() && Objects.nonNull(returnVal)) {
                EventDescriptor eventDescriptor = eventDescriptorParser.get(returnVal);
                publishEventData = eventDescriptor.getEventData(returnVal);
                if (Objects.nonNull(publishEventData)) {
                    long eventDataId = eventDescriptor.getEventDataId(publishEventData);
                    publishIdentity = publishEventRepository.initialize(eventDescriptor.getEventName(), eventDataId, publishEventData);
                }
            }
            transactionManager.commit(transactionStatus);
        } catch (Throwable throwable) {
            transactionManager.rollback(transactionStatus);
            try {
                subscribeEventRepository.markFailed(subscribeIdentity, throwable);
            } catch (Throwable subscribeFailedThrowable) {
                if (log.isErrorEnabled()) {
                    String markFailedError = String.format("invoke - mark subscribe event status to failed error. -> id:[%d] error,taken:[%d]ms!"
                            , subscribeIdentity.getId()
                            , stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    log.error(markFailedError, subscribeFailedThrowable);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("invoke - Subscribe failed! -> id:[{}],taken:[{}]", subscribeIdentity.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
            throw throwable;
        }

        if (log.isDebugEnabled()) {
            log.debug("invoke - Subscribe succeeded! -> id:[{}],taken:[{}]", subscribeIdentity.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
        if (!targetSubscriber.rePublish()) {
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
    public boolean rePublish() {
        return targetSubscriber.rePublish();
    }
}
