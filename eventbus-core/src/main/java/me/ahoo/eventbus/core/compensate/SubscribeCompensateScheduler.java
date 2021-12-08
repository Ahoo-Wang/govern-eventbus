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
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.SubscribeEventEntity;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.simba.core.MutexContendServiceFactory;
import me.ahoo.simba.schedule.ScheduleConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public class SubscribeCompensateScheduler extends AbstractCompensateScheduler {

    private final CompensateConfig compensateConfig;
    private final Deserializer deserializer;
    private final SubscriberRegistry subscriberRegistry;
    protected final SubscribeEventRepository subscribeEventRepository;

    public SubscribeCompensateScheduler(CompensateConfig compensateConfig,
                                        ScheduleConfig scheduleConfig,
                                        Deserializer deserializer,
                                        SubscriberRegistry subscriberRegistry,
                                        SubscribeEventRepository subscribeEventRepository, MutexContendServiceFactory contendServiceFactory) {
        super("eventbus_subscribe_leader", scheduleConfig, contendServiceFactory);
        this.compensateConfig = compensateConfig;
        this.deserializer = deserializer;
        this.subscriberRegistry = subscriberRegistry;
        this.subscribeEventRepository = subscribeEventRepository;
    }

    @Override
    protected String getWorker() {
        return "SubscribeCompensateScheduler";
    }

    @Override
    protected void work() {
        try {
            List<SubscribeEventEntity> failedEvents = subscribeEventRepository.queryFailed(
                    compensateConfig.getBatch(),
                    compensateConfig.getMaxVersion(),
                    compensateConfig.getBefore(),
                    compensateConfig.getRange());
            if (failedEvents.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("work - can not find any failed subscribe event!");
                }
                return;
            }
            for (SubscribeEventEntity failedEvent : failedEvents) {
                compensate(failedEvent);
            }
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    protected void compensate(SubscribeEventEntity failedEntity) {
        SubscribeEventCompensateEntity subscribeEventCompensationEntity = SubscribeEventCompensateEntity.builder()
                .subscribeEventId(failedEntity.getId())
                .startTime(System.currentTimeMillis())
                .build();
        if (log.isInfoEnabled()) {
            log.info("compensate - SubscribeEvent -> id:[{}] ,version:[{}].", failedEntity.getId(), failedEntity.getVersion());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Subscriber subscriber = subscriberRegistry.getSubscriber(failedEntity.getSubscriberName());
        PublishEvent publishEventWrapper = convert(failedEntity, subscriber);

        try {
            subscriber.invoke(publishEventWrapper);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }

            String failedMsg = Throwables.getStackTraceAsString(throwable);
            subscribeEventCompensationEntity.setFailedMsg(failedMsg);
        }

        try {
            long taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            subscribeEventCompensationEntity.setTaken(taken);
            subscribeEventRepository.compensate(subscribeEventCompensationEntity);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    private PublishEvent convert(SubscribeEventEntity subscribeEventEntity, Subscriber subscriber) {
        Object eventTypedData = deserializer.deserialize(subscribeEventEntity.getEventData(), subscriber.getSubscribeEventClass());
        PublishEvent publishEvent = new PublishEvent();
        publishEvent.setId(subscribeEventEntity.getEventId());
        publishEvent.setEventName(subscribeEventEntity.getEventName());
        publishEvent.setEventData(eventTypedData);
        publishEvent.setCreateTime(subscribeEventEntity.getEventCreateTime());
        return publishEvent;
    }

}
