package me.ahoo.eventbus.rabbit;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * @author ahoo wang
 */
@Slf4j
public class RabbitEventListener implements MessageListener {
    private final Subscriber subscriber;
    private final RabbitEventCodec rabbitEventCodec;

    public RabbitEventListener(RabbitEventCodec rabbitEventCodec, Subscriber subscriber) {
        this.rabbitEventCodec = rabbitEventCodec;
        this.subscriber = subscriber;
    }

    @Override
    public void onMessage(Message message) {
        try {
            PublishEvent publishEvent = rabbitEventCodec.decode(message, subscriber.getSubscribeEventClass());
            if (log.isInfoEnabled()) {
                log.info("onMessage - received event subscriber:[{}]-> id:[{}] ,eventName:[{}].", subscriber.getName(), publishEvent.getId(), publishEvent.getEventName());
            }
            this.subscriber.invoke(publishEvent);
        } catch (Throwable throwable) {
            var payloadStr = new String(message.getBody(), Charsets.UTF_8);
            log.error(String.format("onMessage - received event ERROR -> routeKey:[%s] , payload: %n  %s", subscriber.getSubscribeEventName(), payloadStr), throwable);
        }
    }
}
