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

package me.ahoo.eventbus.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.consistency.impl.ConsistencyPublisherImpl;
import me.ahoo.eventbus.core.consistency.impl.ConsistencySubscriberFactoryImpl;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.EventNameGenerator;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventNameGenerator;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.serialize.json.JsonDeserializer;
import me.ahoo.eventbus.core.serialize.json.JsonSerializer;
import me.ahoo.eventbus.core.subscriber.SubscriberNameGenerator;
import me.ahoo.eventbus.core.subscriber.SubscriberScanner;
import me.ahoo.eventbus.core.subscriber.impl.SimpleSubscriberNameGenerator;
import me.ahoo.eventbus.jdbc.JdbcPublishEventRepository;
import me.ahoo.eventbus.jdbc.JdbcSubscribeEventRepository;
import me.ahoo.eventbus.spring.annotation.EnableEventBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : ahoo wang
 */
@Configuration
@EnableEventBus
@EnableConfigurationProperties({EventBusProperties.class})
public class EventBusAutoConfiguration {

    private final EventBusProperties eventBusProperties;

    public EventBusAutoConfiguration(EventBusProperties eventBusProperties) {
        this.eventBusProperties = eventBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventNameGenerator eventNameGenerator() {
        return new SimpleEventNameGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscriberNameGenerator subscriberNameGenerator() {
        return new SimpleSubscriberNameGenerator(eventBusProperties.getSubscriber().getPrefix());
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JavaTimeModule timeModule = new JavaTimeModule();

        timeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        timeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));

        timeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
        timeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));

        objectMapper.registerModule(timeModule);
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer(ObjectMapper objectMapper) {
        return new JsonSerializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Deserializer deserializer(ObjectMapper objectMapper) {
        return new JsonDeserializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventDescriptorParser eventDescriptorParser(EventNameGenerator eventNameGenerator) {
        return new EventDescriptorParser(eventNameGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscriberScanner subscriberScanner(SubscriberNameGenerator subscriberNameGenerator, EventDescriptorParser eventDescriptorParser) {
        return new SubscriberScanner(subscriberNameGenerator, eventDescriptorParser);
    }

    @Bean
    @ConditionalOnMissingBean
    public PublishEventRepository publishEventRepository(Serializer serializer, NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcPublishEventRepository(serializer, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscribeEventRepository subscribeEventRepository(Serializer serializer, NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcSubscribeEventRepository(serializer, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsistencyPublisher consistencyPublisher(Publisher publisher,
                                                     PublishEventRepository publishEventRepository,
                                                     PlatformTransactionManager transactionManager,
                                                     EventDescriptorParser eventDescriptorParser) {
        return new ConsistencyPublisherImpl(publisher, publishEventRepository, transactionManager, eventDescriptorParser);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsistencySubscriberFactory consistencySubscriberFactory(ConsistencyPublisher consistencyPublisher,
                                                                     PublishEventRepository publishEventRepository,
                                                                     SubscribeEventRepository subscribeEventRepository,
                                                                     PlatformTransactionManager transactionManager,
                                                                     EventDescriptorParser eventDescriptorParser) {
        return new ConsistencySubscriberFactoryImpl(consistencyPublisher, publishEventRepository, subscribeEventRepository, transactionManager, eventDescriptorParser);
    }
}
