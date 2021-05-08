package me.ahoo.eventbus.demo.controller;

import me.ahoo.eventbus.demo.event.FieldEventWrapper;
import me.ahoo.eventbus.demo.event.PublishDataEvent;
import me.ahoo.eventbus.demo.service.BusService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ahoo wang
 * createTime 2020/2/17 20:17
 */
@RestController
@RequestMapping("bus")
public class BusController {

    public final BusService busService;
    private final ApplicationContext applicationContext;

    public BusController(BusService busService,
                         ApplicationContext applicationContext) {
        this.busService = busService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("publish")
    public PublishDataEvent publish() {
        applicationContext.getBeansOfType(BusService.class);
        return busService.publish();
    }

    @GetMapping("nestedPublish")
    public PublishDataEvent nestedPublish() {
        return busService.nestedPublish();
    }

    @GetMapping("nestedPublishWithProxy")
    public PublishDataEvent nestedPublishWithProxy() {
        return busService.nestedPublishWithProxy();
    }

    @GetMapping("fieldEventPublish")
    public FieldEventWrapper fieldEventPublish() {
        return busService.fieldEventPublish();
    }

}
