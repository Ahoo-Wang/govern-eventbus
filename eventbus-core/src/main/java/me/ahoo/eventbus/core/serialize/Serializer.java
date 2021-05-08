package me.ahoo.eventbus.core.serialize;

/**
 * @author ahoo wang
 * Creation time 2021/2/1 21:02
 **/
public interface Serializer {

    <T> String serialize(T payloadObj);
}
