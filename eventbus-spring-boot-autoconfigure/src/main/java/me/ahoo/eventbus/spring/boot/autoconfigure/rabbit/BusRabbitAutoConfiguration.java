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

package me.ahoo.eventbus.spring.boot.autoconfigure.rabbit;

import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.rabbit.RabbitEventCodec;
import me.ahoo.eventbus.rabbit.RabbitPublisher;
import me.ahoo.eventbus.rabbit.RabbitSubscriberRegistry;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:34
 */
@EnableConfigurationProperties(RabbitProperties.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@ConditionalOnRabbitEnabled
@ConditionalOnClass(RabbitPublisher.class)
public class BusRabbitAutoConfiguration {

    private final RabbitProperties rabbitProperties;

    public BusRabbitAutoConfiguration(RabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitEventCodec eventCodec(Serializer serializer, Deserializer deserializer) {
        return new RabbitEventCodec(serializer, deserializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Publisher rabbitPublisher(
            RabbitEventCodec rabbitEventCodec,
            ConnectionFactory connectionFactory) {
        return new RabbitPublisher(rabbitEventCodec, rabbitProperties, connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscriberRegistry rabbitSubscriberRegistry(
            RabbitEventCodec rabbitEventCodec,
            ConnectionFactory connectionFactory,
            ConsistencySubscriberFactory subscriberFactory,
            RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry) {
        return new RabbitSubscriberRegistry(rabbitEventCodec, rabbitProperties, connectionFactory, subscriberFactory, rabbitListenerEndpointRegistry);
    }
}
