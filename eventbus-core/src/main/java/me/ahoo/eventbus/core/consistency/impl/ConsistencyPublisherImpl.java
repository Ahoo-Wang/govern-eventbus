package me.ahoo.eventbus.core.consistency.impl;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.utils.Threads;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author ahoo wang
 */
@Slf4j
public class ConsistencyPublisherImpl implements ConsistencyPublisher, AutoCloseable {
    private final int EXECUTOR_CORE_POOL_SIZE = 1;
    private final int EXECUTOR_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final int EXECUTOR_BLOCKING_QUEUE_SIZE = 10000;
    private final Publisher publisher;
    private final EventDescriptorParser eventDescriptorParser;
    private final PublishEventRepository publishEventRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExecutorService executorService;

    public ConsistencyPublisherImpl(Publisher publisher,
                                    EventDescriptorParser eventDescriptorParser,
                                    PublishEventRepository publishEventRepository,
                                    PlatformTransactionManager transactionManager) {

        executorService = new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE, EXECUTOR_MAX_POOL_SIZE,
                10L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(EXECUTOR_BLOCKING_QUEUE_SIZE), Threads.defaultFactory("ConsistencyPublisher"));

        this.publisher = publisher;
        this.eventDescriptorParser = eventDescriptorParser;

        this.publishEventRepository = publishEventRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object publish(Supplier<Object> publishDataSupplier) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        PublishIdentity publishIdentity = null;
        Object returnValue;
        Object publishEventData = null;
        var transactionStatus = transactionManager.getTransaction(null);
        try {
            returnValue = publishDataSupplier.get();
            if (Objects.nonNull(returnValue)) {
                var eventDescriptor = eventDescriptorParser.parse(returnValue);
                publishEventData = eventDescriptor.getEventData(returnValue);
                if (Objects.nonNull(publishEventData)) {
                    publishIdentity = publishEventRepository.initialize(eventDescriptor.getEventName(), publishEventData);
                }
            }
        } catch (Throwable throwable) {
            transactionManager.rollback(transactionStatus);
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            if (log.isWarnEnabled()) {
                log.warn("Publish Inner failed,taken:[{}].error:{}", taken, throwable.getMessage());
            }

            throw throwable;
        }
        this.transactionManager.commit(transactionStatus);
        var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        if (log.isInfoEnabled()) {
            log.info("publish - Inner succeeded,taken:[{}].", taken);
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
                publishEvent.setEventData(publishEventData);
                publishEvent.setCreateTime(publishIdentity.getCreateTime());
            }
            publisher.publish(publishEvent);
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            if (log.isDebugEnabled()) {
                log.debug("doPublish - event to bus succeeded! taken:[{}].", taken);
            }

        } catch (Throwable throwable) {
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            var busError = String.format("doPublish - event to bus error -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), taken);
            log.error(busError, throwable);
            try {
                publishEventRepository.markFailed(publishIdentity, throwable);
            } catch (Throwable publishFailedEx) {
                taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                var markFailedError = String.format("doPublish - mark publish event status to failed error. -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), taken);
                log.error(markFailedError, publishFailedEx);
            }
            throw throwable;
        }

        try {
            publishEventRepository.markSucceeded(publishIdentity);
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            if (log.isDebugEnabled()) {
                log.debug("doPublish - mark publish event to succeeded! taken:[{}].", taken);
            }
        } catch (Throwable throwable) {
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            var markSucceededError = String.format("doPublish - mark publish event to succeeded error. -> id:[%d] error,taken:[%d]!", publishIdentity.getId(), taken);
            log.error(markSucceededError, throwable);
            throw throwable;
        }
        var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        if (log.isDebugEnabled()) {
            log.debug("doPublish - succeeded,taken:[{}]", taken);
        }
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
