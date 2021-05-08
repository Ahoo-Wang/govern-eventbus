package me.ahoo.eventbus.spring.boot.autoconfigure.compensate;

import me.ahoo.eventbus.spring.boot.autoconfigure.EventBusProperties;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:43
 */

public interface CompensatePrefix {
    String PREFIX = EventBusProperties.PREFIX + ".compensate";
}
