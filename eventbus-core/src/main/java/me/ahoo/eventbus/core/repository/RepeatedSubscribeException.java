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

package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.EventBusException;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

/**
 * 重复订阅事件异常.
 *
 * @author ahoo wang
 */
public class RepeatedSubscribeException extends EventBusException {
    private final Subscriber subscriber;
    private final PublishEvent publishEvent;
    private final String errorMsg;
    
    public RepeatedSubscribeException(Subscriber subscriber, PublishEvent publishEvent) {
        this.subscriber = subscriber;
        this.publishEvent = publishEvent;
        errorMsg = String.format("Subscriber.name:[%s] -> id:[%d]", subscriber.getName(), publishEvent.getId());
    }
    
    @Override
    public String getMessage() {
        return errorMsg;
    }
    
    public Subscriber getSubscriber() {
        return subscriber;
    }
    
    public PublishEvent getPublishEvent() {
        return publishEvent;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
}
