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

import lombok.var;
import me.ahoo.eventbus.core.codec.EventCodec;
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author ahoo wang
 * create time 2020/5/14 21:44
 */
public class KafkaEventCodec implements EventCodec {

    private final Serializer serializer;
    private final Deserializer deserializer;

    public KafkaEventCodec(Serializer serializer, Deserializer deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public ProducerRecord<Long, String> encode(PublishEvent publishEvent) {
        String eventDataStr;
        if (publishEvent instanceof CompensatePublishEvent) {
            eventDataStr = ((CompensatePublishEvent) publishEvent).getEventData();
        } else {
            eventDataStr = serializer.serialize(publishEvent.getEventData());
        }

        return new ProducerRecord<>(publishEvent.getEventName(), null, publishEvent.getCreateTime(), publishEvent.getId(), eventDataStr, null);
    }

    public PublishEvent decode(ConsumerRecord<Long, String> consumerRecord, Class<?> eventDataClass) {
        var eventName = consumerRecord.topic();
        var id = consumerRecord.key();
        var timestamp = consumerRecord.timestamp();
        var eventData = deserializer.deserialize(consumerRecord.value(), eventDataClass);
        var publishEvent = new PublishEvent();
        publishEvent.setId(id);
        publishEvent.setEventName(eventName);
        publishEvent.setCreateTime(timestamp);
        publishEvent.setEventData(eventData);
        return publishEvent;
    }
}
