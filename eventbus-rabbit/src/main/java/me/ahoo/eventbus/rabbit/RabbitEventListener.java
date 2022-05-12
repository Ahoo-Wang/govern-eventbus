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

package me.ahoo.eventbus.rabbit;

import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * RabbitEventListener.
 *
 * @author ahoo wang
 */
@Slf4j
public class RabbitEventListener implements MessageListener {
    private final Subscriber subscriber;
    private final RabbitEventCodec rabbitEventCodec;
    
    public RabbitEventListener(RabbitEventCodec rabbitEventCodec, Subscriber subscriber) {
        this.rabbitEventCodec = rabbitEventCodec;
        this.subscriber = subscriber;
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            PublishEvent publishEvent = rabbitEventCodec.decode(message, subscriber.getSubscribeEventClass());
            if (log.isInfoEnabled()) {
                log.info("onMessage - received event subscriber:[{}]-> id:[{}] ,eventName:[{}].", subscriber.getName(), publishEvent.getId(), publishEvent.getEventName());
            }
            this.subscriber.invoke(publishEvent);
        } catch (Throwable throwable) {
            String payloadStr = new String(message.getBody(), Charsets.UTF_8);
            if (log.isErrorEnabled()) {
                log.error(String.format("onMessage - received event ERROR -> routeKey:[%s] , payload: %n  %s", subscriber.getSubscribeEventName(), payloadStr), throwable);
            }
        }
    }
}
