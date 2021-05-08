package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.EventBusException;

/**
 * 并发版本冲突
 *
 * @author ahoo wang
 */
public class ConcurrentVersionConflictException extends EventBusException {

    private final Version version;

    public ConcurrentVersionConflictException(String message, Version version) {
        super(message);
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }
}
