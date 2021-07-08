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

package me.ahoo.eventbus.demo.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.annotation.Publish;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.demo.event.FieldEventData;
import me.ahoo.eventbus.demo.event.FieldEventWrapper;
import me.ahoo.eventbus.demo.event.PublishDataEvent;
import me.ahoo.eventbus.demo.event.RePublishDataEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author ahoo wang
 * createTime 2020/2/17 20:10
 */
@Slf4j
@Service
public class BusService {

    @Autowired
    private BusService proxyBusService;
    private final LongAdder longAdder = new LongAdder();

    /**
     * publish use proxy.
     *
     * @return
     */
    public PublishDataEvent nestedPublishWithProxy() {
        return proxyBusService.publish();
    }

    /**
     * can not publish this.
     *
     * @return
     */
    public PublishDataEvent nestedPublish() {
        log.info("here will can not publish event use event bus.");
        return this.publish();
    }


    @Publish
    public PublishDataEvent publish() {
        log.info("publish");
        longAdder.increment();
        var event = new PublishDataEvent();
        event.setId(longAdder.longValue());
        return event;
    }

    @SneakyThrows
    @Subscribe
    public void subscribePublishDataEvent(PublishDataEvent publishDataEvent) {
        int SLEEP_SECONDS = 5;
        log.info("subscribePublishDataEvent->>id:{} sleep:[{}]", publishDataEvent.getId(), SLEEP_SECONDS);
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);

    }

    @Subscribe
    public PublishDataEvent subscribeThenPublishNull(PublishDataEvent publishDataEvent) {
        log.info("subscribeThenPublishNull->>id:{}", publishDataEvent.getId());
        return null;
    }

    @Subscribe("subscribeThenPublish.customizeQueue")
    public RePublishDataEvent subscribeThenPublish(PublishDataEvent publishDataEvent) {
        log.info("rePublish->>id:{}", publishDataEvent.getId());
        longAdder.increment();
        var event = new RePublishDataEvent();
        event.setId(longAdder.longValue());
        return event;
    }

    @Subscribe
    public void subscribePublishDataEventOther(PublishDataEvent publishDataEvent) {
        log.info("subscribePublishDataEventOther->>id:{}", publishDataEvent.getId());
    }

    @Subscribe
    public void subscribeError(RePublishDataEvent rePublishDataEvent) {
        log.info("subscribeError->>error:{}", rePublishDataEvent.getId());
        throw new RuntimeException("we are error");
    }

    @Publish
    public FieldEventWrapper fieldEventPublish() {
        log.info("fieldEventPublish");
        FieldEventWrapper fieldEventWrapper = new FieldEventWrapper();
        fieldEventWrapper.setResp("any response");
        FieldEventData fieldEventData = new FieldEventData();
        fieldEventData.setId(1L);
        fieldEventWrapper.setFieldEventData(fieldEventData);
        return fieldEventWrapper;
    }

    @Subscribe
    public void subscribeFieldEvent(FieldEventData fieldEventData) {
        log.info("subscribeFieldEvent");
    }
}
