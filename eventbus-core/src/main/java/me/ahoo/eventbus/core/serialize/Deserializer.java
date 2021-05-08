package me.ahoo.eventbus.core.serialize;

/**
 * @author ahoo wang
 * Creation time 2021/2/1 21:02
 **/
public interface Deserializer {

    <T> T deserialize(String payload, Class<T> deserializeType);

}
