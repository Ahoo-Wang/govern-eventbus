package me.ahoo.eventbus.core;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.annotation.Subscribe;

/**
 * @author ahoo wang
 */
@Slf4j
public class DemoSubscriber {

    @Subscribe
    public void subscriber(AnnotationDemoEvent annotationDemoEvent) {
        log.info("subscriber");
    }

    @Subscribe
    public AnnotationDemoEvent subscriberPublish(AnnotationDemoEvent annotationDemoEvent) {
        log.info("subscriberThenPublish");
        return annotationDemoEvent;
    }


    @Subscribe(isPublish = true)
    public DemoEvent subscriberThenPublishUseIsPublish(DemoEvent demoEvent) {
        log.info("subscriberThenPublish");
        return demoEvent;
    }

}
