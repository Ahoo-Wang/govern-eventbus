/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
