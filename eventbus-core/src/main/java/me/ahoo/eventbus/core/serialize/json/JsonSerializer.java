package me.ahoo.eventbus.core.serialize.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.ahoo.eventbus.core.serialize.Serializer;

/**
 * @author ahoo wang
 * Creation time 2021/2/1 22:05
 **/
public class JsonSerializer implements Serializer {
    private final ObjectMapper objectMapper;

    public JsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public String serialize(Object payloadObj) {
        return objectMapper.writeValueAsString(payloadObj);
    }

}
