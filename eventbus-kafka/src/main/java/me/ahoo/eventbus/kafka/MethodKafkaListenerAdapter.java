package me.ahoo.eventbus.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.subscriber.Subscriber;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

import java.lang.reflect.Method;

/**
 * @author ahoo wang
 * Creation time: 2020/4/16 23:08
 */
@Slf4j
public class MethodKafkaListenerAdapter implements MessageListener<Long, String> {

    @Getter
    private final static Method invokeMethod;
    private final KafkaEventCodec kafkaEventCodec;

    static {
        try {
            invokeMethod = MethodKafkaListenerAdapter.class.getMethod("onMessage", ConsumerRecord.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Subscriber subscriber;

    public MethodKafkaListenerAdapter(KafkaEventCodec kafkaEventCodec, Subscriber subscriber) {
        this.kafkaEventCodec = kafkaEventCodec;
        this.subscriber = subscriber;
    }

    @Override
    public void onMessage(ConsumerRecord<Long, String> data) {
        try {
            var publishEvent = kafkaEventCodec.decode(data, subscriber.getSubscribeEventClass());
            if (log.isInfoEnabled()) {
                log.info("onMessage - received event subscriber:[{}]-> id:[{}] ,eventName:[{}].", subscriber.getName(), publishEvent.getId(), publishEvent.getEventName());
            }
            this.subscriber.invoke(publishEvent);
        } catch (Throwable throwable) {
            var payloadStr = data.value();
            log.error(String.format("onMessage - received event ERROR -> routeKey:[%s] , payload: %n  %s", subscriber.getSubscribeEventName(), payloadStr), throwable);
        }
    }
}
