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

package me.ahoo.eventbus.core.repository.entity;

import me.ahoo.eventbus.core.repository.Identity;

import lombok.Builder;

/**
 * PublishEventCompensateEntity.
 *
 * @author ahoo wang
 */
@Builder
public class PublishEventCompensateEntity implements Identity {
    private Long id;
    private Long publishEventId;
    private Long startTime;
    private Long taken;
    private String failedMsg;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPublishEventId() {
        return publishEventId;
    }
    
    public void setPublishEventId(Long publishEventId) {
        this.publishEventId = publishEventId;
    }
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    public Long getTaken() {
        return taken;
    }
    
    public void setTaken(Long taken) {
        this.taken = taken;
    }
    
    public String getFailedMsg() {
        return failedMsg;
    }
    
    public void setFailedMsg(String failedMsg) {
        this.failedMsg = failedMsg;
    }
}
