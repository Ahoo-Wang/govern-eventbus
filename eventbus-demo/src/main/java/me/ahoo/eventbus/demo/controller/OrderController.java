package me.ahoo.eventbus.demo.controller;

import me.ahoo.eventbus.demo.event.OrderCreatedEvent;
import me.ahoo.eventbus.demo.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ahoo wang
 */
@RestController
@RequestMapping("order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderCreatedEvent create() {
        return orderService.createOrder();
    }
}
