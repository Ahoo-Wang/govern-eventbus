package me.ahoo.eventbus.kafka;

import lombok.var;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.publisher.PublishException;
import me.ahoo.eventbus.core.publisher.Publisher;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author ahoo wang
 * create time 2020/5/14 21:44
 */
public class KafkaPublisher implements Publisher {
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final KafkaEventCodec kafkaEventCodec;

    public KafkaPublisher(KafkaEventCodec kafkaEventCodec, KafkaTemplate<Long, String> kafkaTemplate) {
        this.kafkaEventCodec = kafkaEventCodec;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(PublishEvent event) throws PublishException {
        var producerRecord = kafkaEventCodec.encode(event);

        kafkaTemplate.send(producerRecord);
    }
}
