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

import me.ahoo.eventbus.core.codec.EventCodec;
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;

import java.util.Objects;

/**
 * RabbitEventCodec.
 *
 * @author ahoo wang
 */
public class RabbitEventCodec implements EventCodec {
    
    public static final String EVENT_ID = "event_id";
    public static final String EVENT_NAME = "event_name";
    public static final String EVENT_DATA_ID = "event_data_id";
    public static final String EVENT_CREATE_TIME = "event_create_time";
    
    private final Serializer serializer;
    private final Deserializer deserializer;
    
    public RabbitEventCodec(Serializer serializer, Deserializer deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }
    
    public Message encode(PublishEvent publishEvent) {
        String eventName = publishEvent.getEventName();
        
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
            .setHeaderIfAbsent(EVENT_ID, publishEvent.getId())
            .setHeaderIfAbsent(EVENT_DATA_ID, publishEvent.getEventDataId())
            .setHeaderIfAbsent(EVENT_NAME, eventName)
            .setHeaderIfAbsent(EVENT_CREATE_TIME, publishEvent.getCreateTime()).build();
        
        byte[] eventBuff;
        if (publishEvent instanceof CompensatePublishEvent) {
            eventBuff = ((CompensatePublishEvent) publishEvent).getEventData().getBytes(Charsets.UTF_8);
        } else {
            eventBuff = serializer.serialize(publishEvent.getEventData()).getBytes(Charsets.UTF_8);
        }
        return MessageBuilder.withBody(eventBuff).andProperties(messageProperties).build();
    }
    
    public PublishEvent decode(Message message, Class<?> eventDataClass) {
        MessageProperties messageProperties = message.getMessageProperties();
        Long eventId = messageProperties.getHeader(EVENT_ID);
        Preconditions.checkNotNull(eventId, "%s can not be null.", EVENT_ID);
        
        String eventName = messageProperties.getHeader(EVENT_NAME);
        Preconditions.checkNotNull(eventName, "%s can not be null.", EVENT_NAME);
        
        Long eventCreateTime = messageProperties.getHeader(EVENT_CREATE_TIME);
        Preconditions.checkNotNull(eventCreateTime, "%s can not be null.", EVENT_CREATE_TIME);
        
        PublishEvent publishEvent = new PublishEvent();
        publishEvent.setId(eventId);
        publishEvent.setEventName(eventName);
        
        Long eventDataId = messageProperties.getHeader(EVENT_DATA_ID);
        if (Objects.nonNull(eventDataId)) {
            publishEvent.setEventDataId(eventDataId);
        }
        Object typedEventData = deserializer.deserialize(new String(message.getBody(), Charsets.UTF_8), eventDataClass);
        publishEvent.setEventData(typedEventData);
        publishEvent.setCreateTime(eventCreateTime);
        return publishEvent;
        
    }
}
