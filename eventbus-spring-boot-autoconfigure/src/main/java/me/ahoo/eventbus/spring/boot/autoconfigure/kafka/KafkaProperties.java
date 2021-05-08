package me.ahoo.eventbus.spring.boot.autoconfigure.kafka;

import me.ahoo.eventbus.spring.boot.autoconfigure.EventBusProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:38
 */
@ConfigurationProperties(KafkaProperties.PREFIX)
public class KafkaProperties {
    public static final String PREFIX = EventBusProperties.PREFIX + ".kafka";
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
