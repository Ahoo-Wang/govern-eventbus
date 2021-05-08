package me.ahoo.eventbus.spring.boot.autoconfigure.rabbit;

import me.ahoo.eventbus.rabbit.config.RabbitConfig;
import me.ahoo.eventbus.spring.boot.autoconfigure.EventBusProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ahoo wang
 * create time 2020/5/14 22:38
 */
@ConfigurationProperties(RabbitProperties.PREFIX)
public class RabbitProperties extends RabbitConfig {
    public static final String PREFIX = EventBusProperties.PREFIX + ".rabbit";
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
