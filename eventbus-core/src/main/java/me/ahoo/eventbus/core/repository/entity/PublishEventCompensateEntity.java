package me.ahoo.eventbus.core.repository.entity;

import lombok.Builder;
import me.ahoo.eventbus.core.repository.Identity;

/**
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
