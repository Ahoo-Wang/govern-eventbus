package me.ahoo.eventbus.core.publisher;

import me.ahoo.eventbus.core.EventBusException;

/**
 * @author ahoo wang
 */
public class PublishException extends EventBusException {

    public PublishException() {
    }

    public PublishException(String message) {
        super(message);
    }

    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public PublishException(Throwable cause) {
        super(cause);
    }

    public PublishException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
