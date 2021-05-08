package me.ahoo.eventbus.core.serialize.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.ahoo.eventbus.core.serialize.Deserializer;

/**
 * @author ahoo wang
 * Creation time 2021/2/1 22:06
 **/
public class JsonDeserializer implements Deserializer {
    private final ObjectMapper objectMapper;

    public JsonDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public <T> T deserialize(String payload, Class<T> deserializeType) {
        return objectMapper.readValue(payload, deserializeType);
    }

}
