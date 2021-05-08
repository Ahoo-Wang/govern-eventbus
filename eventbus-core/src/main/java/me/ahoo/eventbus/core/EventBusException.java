package me.ahoo.eventbus.core;

/**
 * @author ahoo wang
 */
public class EventBusException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public EventBusException() {
        super();
    }


    public EventBusException(String message) {
        super(message);
    }


    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }


    public EventBusException(Throwable cause) {
        super(cause);
    }


    protected EventBusException(String message, Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
