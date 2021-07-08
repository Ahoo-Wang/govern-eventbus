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

package me.ahoo.eventbus.core.compensate;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public abstract class AbstractSubscribeCompensate implements SubscribeCompensate {
    protected volatile boolean running;
    private final CompensateConfig compensateConfig;
    private final Deserializer deserializer;
    private final SubscriberRegistry subscriberRegistry;
    protected final SubscribeEventRepository subscribeEventRepository;

    protected AbstractSubscribeCompensate(CompensateConfig compensateConfig,
                                          Deserializer deserializer,
                                          SubscriberRegistry subscriberRegistry,
                                          SubscribeEventRepository subscribeEventRepository) {
        this.compensateConfig = compensateConfig;
        this.deserializer = deserializer;
        this.subscriberRegistry = subscriberRegistry;
        this.subscribeEventRepository = subscribeEventRepository;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        start0();
    }

    protected abstract void start0();

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        stop0();
    }

    protected abstract void stop0();

    @Override
    public boolean isRunning() {
        return running;
    }

    protected void schedule() {
        if (log.isInfoEnabled()) {
            log.info("schedule - start.");
        }

        try {
            var failedEntities = queryFailed();
            if (failedEntities.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("schedule - can not find any failed subscribe event!");
                }
                return;
            }
            for (SubscribeEventEntity failedEntity : failedEntities) {
                compensate(failedEntity);
            }
        } catch (Throwable throwable) {
            log.error("schedule - error", throwable);
        }
    }

    protected List<SubscribeEventEntity> queryFailed() {
        return subscribeEventRepository.queryFailed(
                compensateConfig.getBatch(),
                compensateConfig.getBefore(),
                compensateConfig.getMaxVersion());
    }

    protected void compensate(SubscribeEventEntity failedEntity) {
        var subscribeEventCompensationEntity = SubscribeEventCompensateEntity.builder()
                .subscribeEventId(failedEntity.getId())
                .startTime(System.currentTimeMillis())
                .build();
        if (log.isInfoEnabled()) {
            log.info("compensate - SubscribeEvent -> id:[{}] ,version:[{}].", failedEntity.getId(), failedEntity.getVersion());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        var subscriber = subscriberRegistry.getSubscriber(failedEntity.getSubscriberName());
        var publishEventWrapper = convert(failedEntity, subscriber);

        try {
            subscriber.invoke(publishEventWrapper);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            var failedMsg = Throwables.getStackTraceAsString(throwable);
            subscribeEventCompensationEntity.setFailedMsg(failedMsg);
        }

        try {
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            subscribeEventCompensationEntity.setTaken(taken);
            subscribeEventRepository.compensate(subscribeEventCompensationEntity);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
    }

    private PublishEvent convert(SubscribeEventEntity subscribeEventEntity, Subscriber subscriber) {
        var eventTypedData = deserializer.deserialize(subscribeEventEntity.getEventData(), subscriber.getSubscribeEventClass());
        var publishEvent = new PublishEvent();
        publishEvent.setId(subscribeEventEntity.getEventId());
        publishEvent.setEventName(subscribeEventEntity.getEventName());
        publishEvent.setEventData(eventTypedData);
        publishEvent.setCreateTime(subscribeEventEntity.getEventCreateTime());
        return publishEvent;
    }


}
