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

import com.google.common.primitives.Longs;
import me.ahoo.eventbus.core.codec.EventCodec;
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author ahoo wang
 * create time 2020/5/14 21:44
 */
public class KafkaEventCodec implements EventCodec {
    public static final String EVENT_DATA_ID = "event_data_id";
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
        Headers headers = new RecordHeaders();
        headers.add(EVENT_DATA_ID, publishEvent.getEventDataId().toString().getBytes(StandardCharsets.UTF_8));

        return new ProducerRecord<>(publishEvent.getEventName(), null, publishEvent.getCreateTime(), publishEvent.getId(), eventDataStr, headers);
    }

    public PublishEvent decode(ConsumerRecord<Long, String> consumerRecord, Class<?> eventDataClass) {
        String eventName = consumerRecord.topic();
        long id = consumerRecord.key();
        Long timestamp = consumerRecord.timestamp();
        Object eventData = deserializer.deserialize(consumerRecord.value(), eventDataClass);
        Header eventDataIdHeader = consumerRecord.headers().lastHeader(EVENT_DATA_ID);

        PublishEvent publishEvent = new PublishEvent();
        publishEvent.setId(id);
        publishEvent.setEventName(eventName);
        if (Objects.nonNull(eventDataIdHeader)) {
            String eventDataIdStr = new String(eventDataIdHeader.value(), StandardCharsets.UTF_8);
            long eventDataId = Longs.tryParse(eventDataIdStr);
            publishEvent.setEventDataId(eventDataId);
        }
        publishEvent.setCreateTime(timestamp);
        publishEvent.setEventData(eventData);
        return publishEvent;
    }
}
