package me.ahoo.eventbus.core.publisher;

/**
 * @author ahoo wang
 */
public interface EventNameGenerator {

    String generate(Class<?> eventClass);

}
