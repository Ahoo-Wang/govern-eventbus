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

package me.ahoo.eventbus.spring.boot.autoconfigure.compensate;

import me.ahoo.eventbus.core.compensate.PublishCompensateScheduler;
import me.ahoo.eventbus.core.compensate.SubscribeCompensateScheduler;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.simba.core.MutexContendServiceFactory;
import me.ahoo.simba.jdbc.JdbcMutexContendServiceFactory;
import me.ahoo.simba.jdbc.JdbcMutexOwnerRepository;
import me.ahoo.simba.jdbc.MutexOwnerRepository;
import me.ahoo.simba.schedule.ScheduleConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author ahoo wang
 */
@EnableConfigurationProperties(CompensateProperties.class)
@ConditionalOnCompensateEnabled
public class CompensateAutoConfiguration {

    private CompensateProperties compensateProperties;

    public CompensateAutoConfiguration(CompensateProperties compensateProperties) {
        this.compensateProperties = compensateProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public MutexOwnerRepository mutexOwnerRepository(
            DataSource dataSource) {
        return new JdbcMutexOwnerRepository(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(name = "publishMutexContendServiceFactory")
    public MutexContendServiceFactory publishMutexContendServiceFactory(
            MutexOwnerRepository mutexOwnerRepository) {
        return new JdbcMutexContendServiceFactory(mutexOwnerRepository,
                compensateProperties.getPublish().getDbMutex().getInitialDelay(),
                compensateProperties.getPublish().getDbMutex().getTtl(),
                compensateProperties.getPublish().getDbMutex().getTransition());
    }

    @Bean
    @ConditionalOnMissingBean(name = "subscribeMutexContendServiceFactory")
    public MutexContendServiceFactory subscribeMutexContendServiceFactory(
            MutexOwnerRepository mutexOwnerRepository) {
        return new JdbcMutexContendServiceFactory(mutexOwnerRepository,
                compensateProperties.getSubscribe().getDbMutex().getInitialDelay(),
                compensateProperties.getSubscribe().getDbMutex().getTtl(),
                compensateProperties.getSubscribe().getDbMutex().getTransition());
    }

    @Bean
    @ConditionalOnMissingBean
    public PublishCompensateScheduler publishCompensateWorker(
            ConsistencyPublisher consistencyPublisher,
            PublishEventRepository publishEventRepository,
            @Qualifier("publishMutexContendServiceFactory") MutexContendServiceFactory publishMutexContendServiceFactory) {
        CompensateProperties.ScheduleConfig scheduleConfig = compensateProperties.getPublish().getSchedule();
        ScheduleConfig simbaScheduleCfg = ScheduleConfig.ofDelay(scheduleConfig.getInitialDelay(), scheduleConfig.getPeriod());
        return new PublishCompensateScheduler(compensateProperties.getPublish(),
                simbaScheduleCfg,
                consistencyPublisher,
                publishEventRepository, publishMutexContendServiceFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscribeCompensateScheduler subscribeCompensateWorker(
            Deserializer deserializer,
            SubscriberRegistry subscriberRegistry,
            SubscribeEventRepository subscribeEventRepository,
            @Qualifier("subscribeMutexContendServiceFactory") MutexContendServiceFactory subscribeMutexContendServiceFactory) {
        CompensateProperties.ScheduleConfig scheduleConfig = compensateProperties.getPublish().getSchedule();
        ScheduleConfig simbaScheduleCfg = ScheduleConfig.ofDelay(scheduleConfig.getInitialDelay(), scheduleConfig.getPeriod());
        return new SubscribeCompensateScheduler(compensateProperties.getSubscribe(),
                simbaScheduleCfg,
                deserializer, subscriberRegistry, subscribeEventRepository, subscribeMutexContendServiceFactory);
    }
}
