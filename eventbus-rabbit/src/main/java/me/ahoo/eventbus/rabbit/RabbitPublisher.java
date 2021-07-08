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

package me.ahoo.eventbus.rabbit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.publisher.PublishException;
import me.ahoo.eventbus.core.publisher.Publisher;
import me.ahoo.eventbus.rabbit.config.RabbitConfig;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author ahoo wang
 */
@Slf4j
public class RabbitPublisher implements Publisher, AutoCloseable {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitEventCodec rabbitEventCodec;

    @SneakyThrows
    public RabbitPublisher(RabbitEventCodec rabbitEventCodec, RabbitConfig rabbitConfig, ConnectionFactory connectionFactory) {
        this.rabbitEventCodec = rabbitEventCodec;
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate.setExchange(rabbitConfig.getExchange());
    }

    @SneakyThrows
    @Override
    public void publish(PublishEvent publishEvent) {
        try {
            var eventName = publishEvent.getEventName();
            var message = rabbitEventCodec.encode(publishEvent);

            rabbitTemplate.send(eventName, message);
            if (log.isInfoEnabled()) {
                log.info("publish - eventName:[{}] -> eventId:[{}] ", eventName, publishEvent.getId());
            }
        } catch (Throwable throwable) {
            throw new PublishException(throwable);
        }
    }

    @Override
    public void close() throws Exception {
        log.info("close - closing resources!");
        rabbitTemplate.destroy();
    }
}
