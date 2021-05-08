package me.ahoo.eventbus.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author : ahoo wang
 */
@ConfigurationProperties(EventBusProperties.PREFIX)
public class EventBusProperties {
    public static final String PREFIX = "govern.eventbus";
    @NestedConfigurationProperty
    private Subscriber subscriber;

    public EventBusProperties() {
        subscriber = new Subscriber();
    }

    public static class Subscriber {
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }
}
