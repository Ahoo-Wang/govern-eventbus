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
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.publisher.EventDescriptor;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.utils.Threads;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author ahoo wang
 */
@Slf4j
public class ConsistencyPublisherImpl implements ConsistencyPublisher, AutoCloseable {
    private static final int EXECUTOR_CORE_POOL_SIZE = 1;
    private static final int EXECUTOR_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int EXECUTOR_BLOCKING_QUEUE_SIZE = 10000;
    private final Publisher publisher;

    private final PublishEventRepository publishEventRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExecutorService executorService;
    private final EventDescriptorParser eventDescriptorParser;

    public ConsistencyPublisherImpl(Publisher publisher,
                                    PublishEventRepository publishEventRepository,
                                    PlatformTransactionManager transactionManager, EventDescriptorParser eventDescriptorParser) {
        this.eventDescriptorParser = eventDescriptorParser;

        executorService = new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE, EXECUTOR_MAX_POOL_SIZE,
                1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(EXECUTOR_BLOCKING_QUEUE_SIZE), Threads.defaultFactory("ConsistencyPublisher"));

        this.publisher = publisher;
        this.publishEventRepository = publishEventRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object publish(Supplier<Object> publishDataSupplier) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        PublishIdentity publishIdentity = null;
        Object returnValue;
        Object publishEventData = null;
        TransactionStatus transactionStatus = transactionManager.getTransaction(null);
        try {
            returnValue = publishDataSupplier.get();
            if (Objects.nonNull(returnValue)) {
                EventDescriptor eventDescriptor = eventDescriptorParser.get(returnValue);
                publishEventData = eventDescriptor.getEventData(returnValue);
                if (Objects.nonNull(publishEventData)) {
                    long eventDataId = eventDescriptor.getEventDataId(publishEventData);
                    publishIdentity = publishEventRepository.initialize(eventDescriptor.getEventName(), eventDataId, publishEventData);
                }
            }
            this.transactionManager.commit(transactionStatus);
        } catch (Throwable throwable) {
            transactionManager.rollback(transactionStatus);
            if (log.isWarnEnabled()) {
                log.warn("Publish Inner failed,taken:[{}].error:{}", stopwatch.elapsed(TimeUnit.MILLISECONDS), throwable.getMessage());
            }
            throw throwable;
        }

        if (log.isInfoEnabled()) {
            log.info("publish - Inner succeeded,taken:[{}].", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }

        if (Objects.isNull(publishIdentity)) {
            if (log.isInfoEnabled()) {
                log.info("publish - Ignore publish event when publishEvent is null.");
            }
            return returnValue;
        }

        publish(publishIdentity, publishEventData);

        return returnValue;
    }

    @Override
    public Future<?> publish(PublishIdentity publishIdentity, Object publishEventData) {
        return executorService.submit(() -> doPublish(publishIdentity, publishEventData));
    }

    private void doPublish(PublishIdentity publishIdentity, Object publishEventData) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            PublishEvent publishEvent;
            if (publishEventData instanceof CompensatePublishEvent) {
                publishEvent = (CompensatePublishEvent) publishEventData;
            } else {
                publishEvent = new PublishEvent();
                publishEvent.setId(publishIdentity.getId());
                publishEvent.setEventName(publishIdentity.getEventName());
                publishEvent.setEventDataId(publishIdentity.getEventDataId());
                publishEvent.setEventData(publishEventData);
                publishEvent.setCreateTime(publishIdentity.getCreateTime());
            }
            publisher.publish(publishEvent);
            if (log.isDebugEnabled()) {
                log.debug("doPublish - event to bus succeeded! taken:[{}].", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                String busError = String.format("doPublish - event to bus error -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
                log.error(busError, throwable);
            }
            try {
                publishEventRepository.markFailed(publishIdentity, throwable);
            } catch (Throwable publishFailedEx) {
                if (log.isErrorEnabled()) {
                    String markFailedError = String.format("doPublish - mark publish event status to failed error. -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    log.error(markFailedError, publishFailedEx);
                }
            }
            return;
        }

        try {
            publishEventRepository.markSucceeded(publishIdentity);
            if (log.isDebugEnabled()) {
                log.debug("doPublish - mark publish event to succeeded! taken:[{}].", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                String markSucceededError = String.format("doPublish - mark publish event to succeeded error. -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
                log.error(markSucceededError, throwable);
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("doPublish - succeeded,taken:[{}]", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
