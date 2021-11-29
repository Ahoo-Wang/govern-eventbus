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

package me.ahoo.eventbus.core.annotation;

import me.ahoo.eventbus.core.publisher.EventDataIdGetter;

import java.lang.annotation.*;

/**
 * @author ahoo wang
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Event {
    /**
     * event name
     * <p>
     * kafka:topic
     * rabbit:routeKey
     *
     * @return event name
     */
    String value() default "";

    /**
     * event data id's field name
     *
     * @return event data id's field name
     */
    String dataId() default EventDataIdGetter.DEFAULT_ID_FIELD_NAME;
}
