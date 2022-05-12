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
import me.ahoo.eventbus.core.publisher.PublishException;
import me.ahoo.eventbus.core.publisher.Publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * KafkaPublisher.
 *
 * @author ahoo wang
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
        ProducerRecord<Long, String> producerRecord = kafkaEventCodec.encode(event);
        
        kafkaTemplate.send(producerRecord);
    }
}
