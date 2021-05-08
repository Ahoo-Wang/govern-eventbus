package me.ahoo.eventbus.demo.service;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

/**
 * @author ahoo wang
 */
@Slf4j
@Service
public class NoticeService {

    @Subscribe
    public void handleOrderCreated(OrderCreatedEvent orderCreatedEvent) {
        log.info("handleOrderCreated - event:[{}].", orderCreatedEvent);
        /**
         * Execute business code
         * send sms / email ?
         */
    }
}
