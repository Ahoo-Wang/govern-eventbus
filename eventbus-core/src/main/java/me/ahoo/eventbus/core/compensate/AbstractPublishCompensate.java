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
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.PublishIdentity;
import me.ahoo.eventbus.core.repository.entity.PublishEventCompensateEntity;
import me.ahoo.eventbus.core.repository.entity.PublishEventEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public abstract class AbstractPublishCompensate implements PublishCompensate {

    protected volatile boolean running;
    private final CompensateConfig compensateConfig;
    private final ConsistencyPublisher consistencyPublisher;
    protected final PublishEventRepository publishEventRepository;

    protected AbstractPublishCompensate(
            CompensateConfig compensateConfig,
            ConsistencyPublisher consistencyPublisher,
            PublishEventRepository publishEventRepository) {
        this.compensateConfig = compensateConfig;
        this.consistencyPublisher = consistencyPublisher;
        this.publishEventRepository = publishEventRepository;
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
        if (log.isDebugEnabled()) {
            log.debug("schedule - start.");
        }

        try {
            var failedEvents = queryFailed();
            if (failedEvents.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("schedule - can not find any failed publish event!");
                }
                return;
            }
            for (PublishEventEntity failedEvent : failedEvents) {
                compensate(failedEvent);
            }
        } catch (Throwable throwable) {
            log.error("schedule - error", throwable);
        }
    }

    protected List<PublishEventEntity> queryFailed() {
        return publishEventRepository.queryFailed(
                compensateConfig.getBatch(),
                compensateConfig.getBefore(),
                compensateConfig.getMaxVersion());
    }

    protected void compensate(PublishEventEntity failedEvent) {
        var publishEventCompensationEntity = PublishEventCompensateEntity.builder()
                .publishEventId(failedEvent.getId())
                .startTime(System.currentTimeMillis())
                .build();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (log.isInfoEnabled()) {
                log.info("compensate - PublishEvent -> id:[{}] ,version:[{}].", failedEvent.getId(), failedEvent.getVersion());
            }

            var publishIdentity = new PublishIdentity();
            publishIdentity.setId(failedEvent.getId());
            publishIdentity.setEventName(failedEvent.getEventName());
            publishIdentity.setStatus(failedEvent.getStatus());
            publishIdentity.setVersion(failedEvent.getVersion());

            CompensatePublishEvent compensatePublishEvent = new CompensatePublishEvent();
            compensatePublishEvent.setId(failedEvent.getId());
            compensatePublishEvent.setEventName(failedEvent.getEventName());
            compensatePublishEvent.setEventData(failedEvent.getEventData());
            compensatePublishEvent.setCreateTime(failedEvent.getCreateTime());
            consistencyPublisher.publish(publishIdentity, compensatePublishEvent).get();
        } catch (Throwable throwable) {
            var failedMsg = Throwables.getStackTraceAsString(throwable);
            publishEventCompensationEntity.setFailedMsg(failedMsg);
        }

        try {
            var taken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            publishEventCompensationEntity.setTaken(taken);
            publishEventRepository.compensate(publishEventCompensationEntity);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
    }

}
