package me.ahoo.eventbus.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.var;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import me.ahoo.eventbus.core.serialize.json.JsonDeserializer;
import me.ahoo.eventbus.core.serialize.json.JsonSerializer;
import me.ahoo.eventbus.spring.annotation.EnableEventBus;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.consistency.ConsistencySubscriberFactory;
import me.ahoo.eventbus.core.consistency.impl.ConsistencyPublisherImpl;
import me.ahoo.eventbus.core.consistency.impl.ConsistencySubscriberFactoryImpl;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.EventNameGenerator;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventDescriptorParser;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventNameGenerator;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.subscriber.SubscriberNameGenerator;
import me.ahoo.eventbus.core.subscriber.SubscriberScanner;
import me.ahoo.eventbus.core.subscriber.impl.SimpleSubscriberNameGenerator;
import me.ahoo.eventbus.jdbc.JdbcPublishEventRepository;
import me.ahoo.eventbus.jdbc.JdbcSubscribeEventRepository;
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
        var objectMapper = new ObjectMapper();
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
        return new SimpleEventDescriptorParser(eventNameGenerator);
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
                                                     EventDescriptorParser eventDescriptorParser,
                                                     PublishEventRepository publishEventRepository,
                                                     PlatformTransactionManager transactionManager) {
        return new ConsistencyPublisherImpl(publisher, eventDescriptorParser, publishEventRepository, transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsistencySubscriberFactory consistencySubscriberFactory(ConsistencyPublisher consistencyPublisher,
                                                                     EventDescriptorParser eventDescriptorParser,
                                                                     PublishEventRepository publishEventRepository,
                                                                     SubscribeEventRepository subscribeEventRepository,
                                                                     PlatformTransactionManager transactionManager) {
        return new ConsistencySubscriberFactoryImpl(consistencyPublisher, eventDescriptorParser, publishEventRepository, subscribeEventRepository, transactionManager);
    }
}
