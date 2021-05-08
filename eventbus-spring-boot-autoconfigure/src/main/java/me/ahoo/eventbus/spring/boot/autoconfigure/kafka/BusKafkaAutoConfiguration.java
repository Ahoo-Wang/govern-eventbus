package me.ahoo.eventbus.spring.boot.autoconfigure.kafka;

import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.kafka.KafkaEventCodec;
import me.ahoo.eventbus.kafka.KafkaPublisher;
import me.ahoo.eventbus.kafka.KafkaSubscriberRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:34
 */
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaPublisher.class)
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnKafkaEnabled
public class BusKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventCodec eventCodec(Serializer serializer, Deserializer deserializer) {
        return new KafkaEventCodec(serializer, deserializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Publisher kafkaPublisher(KafkaEventCodec kafkaEventCodec,
                                    KafkaTemplate<Long, String> kafkaTemplate) {
        return new KafkaPublisher(kafkaEventCodec, kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscriberRegistry kafkaSubscriberRegistry(
            KafkaEventCodec kafkaEventCodec,
            ConsistencySubscriberFactory consistencySubscriberFactory,
            KafkaListenerEndpointRegistry listenerEndpointRegistry,
            KafkaListenerContainerFactory listenerContainerFactory) {
        return new KafkaSubscriberRegistry(kafkaEventCodec, consistencySubscriberFactory, listenerEndpointRegistry, listenerContainerFactory);
    }
}
