package me.ahoo.eventbus.core.publisher;

/**
 * @author ahoo wang
 */
public interface Publisher {
    /**
     * @param event
     * @throws PublishException
     */
    void publish(PublishEvent event) throws PublishException;
}

