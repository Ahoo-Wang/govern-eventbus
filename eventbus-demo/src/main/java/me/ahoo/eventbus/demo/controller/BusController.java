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

package me.ahoo.eventbus.demo.controller;

import me.ahoo.eventbus.demo.event.FieldEventWrapper;
import me.ahoo.eventbus.demo.event.PublishDataEvent;
import me.ahoo.eventbus.demo.service.BusService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BusController.
 *
 * @author ahoo wang
 */
@RestController
@RequestMapping("bus")
public class BusController {
    
    public final BusService busService;
    
    public BusController(BusService busService) {
        this.busService = busService;
    }
    
    @GetMapping("publish")
    public PublishDataEvent publish() {
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
