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

import me.ahoo.eventbus.core.annotation.Event;
import me.ahoo.eventbus.core.publisher.EventDescriptor;
import me.ahoo.eventbus.core.publisher.EventDescriptorParser;
import me.ahoo.eventbus.core.publisher.impl.SimpleEventNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author ahoo wang
 */
class EventDescriptorParserTest {
    private final EventDescriptorParser eventDescriptorParser = new EventDescriptorParser(new SimpleEventNameGenerator());

    @Test
    void get_EventOfNoneAnnotation() {
        EventDescriptor eventDescriptor = eventDescriptorParser.get(EventOfNoneAnnotation.class);
        Assertions.assertNotNull(eventDescriptor);
        Assertions.assertEquals("EventOfNoneAnnotation", eventDescriptor.getEventName());
        Assertions.assertEquals(0, eventDescriptor.getEventDataId(null));
    }

    @Test
    void get_EventOfAnnotation() {
        EventDescriptor eventDescriptor = eventDescriptorParser.get(EventOfAnnotation.class);
        Assertions.assertNotNull(eventDescriptor);
        Assertions.assertEquals("EventDescriptorSupportTest-EventOfAnnotation", eventDescriptor.getEventName());
        Assertions.assertEquals(0, eventDescriptor.getEventDataId(null));
    }

    @Test
    void get_EventOfFieldAnnotation() {
        EventDescriptor eventDescriptor = eventDescriptorParser.get(EventOfFieldAnnotation.class);
        Assertions.assertNotNull(eventDescriptor);
        Assertions.assertEquals("EventDescriptorSupportTest-EventOfFieldAnnotation", eventDescriptor.getEventName());
        Assertions.assertEquals(0, eventDescriptor.getEventDataId(null));
    }

    @Test
    void get_EventOfEventWithId() {
        EventWithId eventWithId = new EventWithId();
        eventWithId.setEventDataId(10);
        EventDescriptor eventDescriptor = eventDescriptorParser.get(eventWithId);
        Assertions.assertNotNull(eventDescriptor);
        Assertions.assertEquals("EventWithId", eventDescriptor.getEventName());
        Assertions.assertEquals(10, eventDescriptor.getEventDataId(eventWithId));
    }

    @Test
    void get_EventWithIdOfDefault() {
        EventWithIdOfDefault eventWithId = new EventWithIdOfDefault();
        eventWithId.setId(10);
        EventDescriptor eventDescriptor = eventDescriptorParser.get(eventWithId);
        Assertions.assertNotNull(eventDescriptor);
        Assertions.assertEquals("EventWithIdOfDefault", eventDescriptor.getEventName());
        Assertions.assertEquals(10, eventDescriptor.getEventDataId(eventWithId));
    }

    public class EventOfNoneAnnotation {

    }

    @Event("EventDescriptorSupportTest-EventOfAnnotation")
    public class EventOfAnnotation {

    }


    public class EventOfFieldAnnotation {
        @Event("EventDescriptorSupportTest-EventOfFieldAnnotation")
        private EventOfNoneAnnotation data;

        public EventOfNoneAnnotation getData() {
            return data;
        }

        public void setData(EventOfNoneAnnotation data) {
            this.data = data;
        }
    }

    @Event(dataId = "eventDataId")
    public class EventWithId {
        private long eventDataId;

        public long getEventDataId() {
            return eventDataId;
        }

        public void setEventDataId(long eventDataId) {
            this.eventDataId = eventDataId;
        }
    }

    public class EventWithIdOfDefault {
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
