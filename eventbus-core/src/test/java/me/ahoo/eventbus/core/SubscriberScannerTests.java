package me.ahoo.eventbus.core;

import lombok.var;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.EventNameGenerator;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventDescriptorParser;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventNameGenerator;
import me.ahoo.eventbus.core.subscriber.SubscriberNameGenerator;
import me.ahoo.eventbus.core.subscriber.SubscriberScanner;
import me.ahoo.eventbus.core.subscriber.impl.SimpleSubscriberNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author ahoo wang
 */
public class SubscriberScannerTests {

    private final SubscriberScanner subscriberScanner;

    public SubscriberScannerTests() {
        EventNameGenerator eventNameGenerator = new SimpleEventNameGenerator();
        EventDescriptorParser eventDescriptorParser = new SimpleEventDescriptorParser(eventNameGenerator);
        SubscriberNameGenerator subscriberNameGenerator = new SimpleSubscriberNameGenerator("eventbus-");
        this.subscriberScanner = new SubscriberScanner(subscriberNameGenerator, eventDescriptorParser);
    }


    @Test
    public void scan() {
        var list = subscriberScanner.scan(new DemoSubscriber());
        Assertions.assertNotNull(list);
    }
}
