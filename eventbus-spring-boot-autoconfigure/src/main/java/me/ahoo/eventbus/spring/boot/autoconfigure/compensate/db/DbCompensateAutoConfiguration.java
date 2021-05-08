package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.db;

import me.ahoo.eventbus.core.compensate.PublishCompensate;
import me.ahoo.eventbus.core.compensate.SubscribeCompensate;
import me.ahoo.eventbus.core.compensate.db.DbPublishCompensate;
import me.ahoo.eventbus.core.compensate.db.DbSubscribeCompensate;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author ahoo wang
 */
@EnableConfigurationProperties(DbCompensateProperties.class)
@ConditionalOnDbCompensateEnabled
public class DbCompensateAutoConfiguration {
    private DbCompensateProperties dbCompensateProperties;

    public DbCompensateAutoConfiguration(DbCompensateProperties dbCompensateProperties) {
        this.dbCompensateProperties = dbCompensateProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public PublishCompensate publishCompensation(
            ConsistencyPublisher consistencyPublisher,
            PublishEventRepository publishEventRepository) {
        return new DbPublishCompensate(dbCompensateProperties.getPublish(), consistencyPublisher, publishEventRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscribeCompensate subscribeCompensation(
            Deserializer deserializer,
            SubscriberRegistry subscriberRegistry,
            SubscribeEventRepository subscribeEventRepository) {
        return new DbSubscribeCompensate(deserializer, dbCompensateProperties.getSubscribe(), subscriberRegistry, subscribeEventRepository);
    }
}
