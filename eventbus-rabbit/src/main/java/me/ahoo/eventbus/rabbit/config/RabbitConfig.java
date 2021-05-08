package me.ahoo.eventbus.rabbit.config;

import me.ahoo.eventbus.core.Consts;

/**
 * @author ahoo wang
 */
public class RabbitConfig {
    private String exchange = Consts.GOVERN_EVENTBUS;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

}
