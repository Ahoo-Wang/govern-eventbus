package me.ahoo.eventbus.rabbit;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import lombok.var;
import me.ahoo.eventbus.core.codec.EventCodec;
import me.ahoo.eventbus.core.compensate.CompensatePublishEvent;
import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.serialize.Serializer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;


/**
 * @author ahoo wang
 */
public class RabbitEventCodec implements EventCodec {

    public static final String EVENT_ID = "event_id";
    public static final String EVENT_NAME = "event_name";
    public static final String EVENT_CREATE_TIME = "event_create_time";
    private final Serializer serializer;
    private final Deserializer deserializer;

    public RabbitEventCodec(Serializer serializer, Deserializer deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public Message encode(PublishEvent publishEvent) {
        var eventName = publishEvent.getEventName();

        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
                .setHeaderIfAbsent(EVENT_ID, publishEvent.getId())
                .setHeaderIfAbsent(EVENT_NAME, eventName)
                .setHeaderIfAbsent(EVENT_CREATE_TIME, publishEvent.getCreateTime()).build();

        byte[] eventBuff;
        if (publishEvent instanceof CompensatePublishEvent) {
            eventBuff = ((CompensatePublishEvent) publishEvent).getEventData().getBytes(Charsets.UTF_8);
        } else {
            eventBuff = serializer.serialize(publishEvent.getEventData()).getBytes(Charsets.UTF_8);
        }
        return MessageBuilder.withBody(eventBuff).andProperties(messageProperties).build();
    }

    public PublishEvent decode(Message message, Class<?> eventDataClass) {
        var messageProperties = message.getMessageProperties();
        Long eventId = messageProperties.getHeader(EVENT_ID);
        Preconditions.checkNotNull(eventId, "%s can not be null.", EVENT_ID);

        String eventName = messageProperties.getHeader(EVENT_NAME);
        Preconditions.checkNotNull(eventName, "%s can not be null.", EVENT_NAME);

        Long eventCreateTime = messageProperties.getHeader(EVENT_CREATE_TIME);
        Preconditions.checkNotNull(eventCreateTime, "%s can not be null.", EVENT_CREATE_TIME);

        var typedEventData = deserializer.deserialize(new String(message.getBody(), Charsets.UTF_8), eventDataClass);
        var publishEvent = new PublishEvent();
        publishEvent.setId(eventId);
        publishEvent.setEventName(eventName);
        publishEvent.setEventData(typedEventData);
        publishEvent.setCreateTime(eventCreateTime);
        return publishEvent;

    }
}
