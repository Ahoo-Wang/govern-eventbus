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
