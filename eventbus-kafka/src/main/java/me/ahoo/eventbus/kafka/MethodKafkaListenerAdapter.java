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

package me.ahoo.eventbus.kafka;

import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

import java.lang.reflect.Method;

/**
 * MethodKafkaListenerAdapter.
 *
 * @author ahoo wang
 */
@Slf4j
public class MethodKafkaListenerAdapter implements MessageListener<Long, String> {
    
    public static final Method INVOKE_METHOD;
    private final KafkaEventCodec kafkaEventCodec;
    
    static {
        try {
            INVOKE_METHOD = MethodKafkaListenerAdapter.class.getMethod("onMessage", ConsumerRecord.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final Subscriber subscriber;
    
    public MethodKafkaListenerAdapter(KafkaEventCodec kafkaEventCodec, Subscriber subscriber) {
        this.kafkaEventCodec = kafkaEventCodec;
        this.subscriber = subscriber;
    }
    
    @Override
    public void onMessage(ConsumerRecord<Long, String> data) {
        try {
            PublishEvent publishEvent = kafkaEventCodec.decode(data, subscriber.getSubscribeEventClass());
            if (log.isInfoEnabled()) {
                log.info("onMessage - received event subscriber:[{}]-> id:[{}] ,eventName:[{}].", subscriber.getName(), publishEvent.getId(), publishEvent.getEventName());
            }
            this.subscriber.invoke(publishEvent);
        } catch (Throwable throwable) {
            String payloadStr = data.value();
            log.error(String.format("onMessage - received event ERROR -> routeKey:[%s] , payload: %n  %s", subscriber.getSubscribeEventName(), payloadStr), throwable);
        }
    }
}
