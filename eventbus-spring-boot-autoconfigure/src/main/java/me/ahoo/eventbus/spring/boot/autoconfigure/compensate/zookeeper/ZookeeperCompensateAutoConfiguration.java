package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.zookeeper;

import me.ahoo.eventbus.core.compensate.PublishCompensate;
import me.ahoo.eventbus.core.compensate.SubscribeCompensate;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.zookeeper.ZookeeperPublishCompensate;
import me.ahoo.eventbus.zookeeper.ZookeeperSubscribeCompensate;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author ahoo wang
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnClass(ZookeeperPublishCompensate.class)
@ConditionalOnZookeeperCompensateEnabled
@EnableConfigurationProperties(ZookeeperCompensateProperties.class)
public class ZookeeperCompensateAutoConfiguration {
    private final ZookeeperCompensateProperties zookeeperCompensationProperties;

    public ZookeeperCompensateAutoConfiguration(ZookeeperCompensateProperties zookeeperCompensationProperties) {
        this.zookeeperCompensationProperties = zookeeperCompensationProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public PublishCompensate publishCompensation(
            CuratorFramework curatorFramework,
            ConsistencyPublisher consistencyPublisher,
            PublishEventRepository publishEventRepository) {
        return new ZookeeperPublishCompensate(zookeeperCompensationProperties.getPublish(), curatorFramework, consistencyPublisher, publishEventRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscribeCompensate subscribeCompensation(
            CuratorFramework curatorFramework,
            Deserializer deserializer,
            SubscriberRegistry subscriberRegistry,
            SubscribeEventRepository subscribeEventRepository) {
        return new ZookeeperSubscribeCompensate(zookeeperCompensationProperties.getSubscribe(), curatorFramework, deserializer, subscriberRegistry, subscribeEventRepository);
    }
}
