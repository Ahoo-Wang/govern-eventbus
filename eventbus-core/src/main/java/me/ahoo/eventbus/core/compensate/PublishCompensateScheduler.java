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
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.repository.entity.PublishEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.PublishEventEntity;
import me.ahoo.simba.core.MutexContendServiceFactory;
import me.ahoo.simba.schedule.ScheduleConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public class PublishCompensateScheduler extends AbstractCompensateScheduler {

    private final CompensateConfig compensateConfig;
    private final ConsistencyPublisher consistencyPublisher;
    protected final PublishEventRepository publishEventRepository;

    public PublishCompensateScheduler(CompensateConfig compensateConfig,
                                      ScheduleConfig scheduleConfig,
                                      ConsistencyPublisher consistencyPublisher,
                                      PublishEventRepository publishEventRepository,
                                      MutexContendServiceFactory contendServiceFactory) {
        super("eventbus_publish_leader", scheduleConfig, contendServiceFactory);
        this.compensateConfig = compensateConfig;
        this.consistencyPublisher = consistencyPublisher;
        this.publishEventRepository = publishEventRepository;
    }

    @Override
    protected String getWorker() {
        return "PublishCompensateScheduler";
    }

    @Override
    protected void work() {
        try {
            List<PublishEventEntity> failedEvents = publishEventRepository.queryFailed(
                    compensateConfig.getBatch(),
                    compensateConfig.getMaxVersion(),
                    compensateConfig.getBefore(),
                    compensateConfig.getRange());
            if (failedEvents.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("work - can not find any failed publish event!");
                }
                return;
            }
            for (PublishEventEntity failedEvent : failedEvents) {
                compensate(failedEvent);
            }
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    protected void compensate(PublishEventEntity failedEvent) {
        PublishEventCompensateEntity publishEventCompensationEntity = PublishEventCompensateEntity.builder()
                .publishEventId(failedEvent.getId())
                .startTime(System.currentTimeMillis())
                .build();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (log.isInfoEnabled()) {
                log.info("compensate - PublishEvent -> id:[{}] ,version:[{}].", failedEvent.getId(), failedEvent.getVersion());
            }

            PublishIdentity publishIdentity = new PublishIdentity();
            publishIdentity.setId(failedEvent.getId());
            publishIdentity.setEventName(failedEvent.getEventName());
            publishIdentity.setStatus(failedEvent.getStatus());
            publishIdentity.setVersion(failedEvent.getVersion());
            publishIdentity.setCreateTime(failedEvent.getCreateTime());

            CompensatePublishEvent compensatePublishEvent = new CompensatePublishEvent();
            compensatePublishEvent.setId(failedEvent.getId());
            compensatePublishEvent.setEventName(failedEvent.getEventName());
            compensatePublishEvent.setEventData(failedEvent.getEventData());
            compensatePublishEvent.setCreateTime(failedEvent.getCreateTime());
            consistencyPublisher.publish(publishIdentity, compensatePublishEvent).get();
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }
            String failedMsg = Throwables.getStackTraceAsString(throwable);
            publishEventCompensationEntity.setFailedMsg(failedMsg);
        }

        try {
            long taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            publishEventCompensationEntity.setTaken(taken);
            publishEventRepository.compensate(publishEventCompensationEntity);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

}
