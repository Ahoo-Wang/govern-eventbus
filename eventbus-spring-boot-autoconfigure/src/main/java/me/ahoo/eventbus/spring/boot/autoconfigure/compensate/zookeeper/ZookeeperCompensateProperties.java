package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.zookeeper;

import me.ahoo.eventbus.spring.boot.autoconfigure.compensate.CompensatePrefix;
import me.ahoo.eventbus.zookeeper.config.PublishConfig;
import me.ahoo.eventbus.zookeeper.config.SubscribeConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ahoo wang
 */
@ConfigurationProperties(ZookeeperCompensateProperties.PREFIX)
public class ZookeeperCompensateProperties {
    public final static String PREFIX = CompensatePrefix.PREFIX + ".zookeeper";
    private boolean enabled = false;
    @NestedConfigurationProperty
    private PublishConfig publish;
    @NestedConfigurationProperty
    private SubscribeConfig subscribe;

    public ZookeeperCompensateProperties() {
        publish = new PublishConfig();
        subscribe = new SubscribeConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PublishConfig getPublish() {
        return publish;
    }

    public void setPublish(PublishConfig publish) {
        this.publish = publish;
    }

    public SubscribeConfig getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(SubscribeConfig subscribe) {
        this.subscribe = subscribe;
    }
}
