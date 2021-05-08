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
