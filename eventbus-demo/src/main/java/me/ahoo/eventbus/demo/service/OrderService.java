package me.ahoo.eventbus.demo.service;

import me.ahoo.eventbus.core.annotation.Publish;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

/**
 * @author ahoo wang
 */
@Service
public class OrderService {

    @Publish
    public OrderCreatedEvent createOrder() {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrderId(1L);
        return orderCreatedEvent;
    }
}
