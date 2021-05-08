package me.ahoo.eventbus.core.repository.entity;

import me.ahoo.eventbus.core.publisher.PublishEvent;
import me.ahoo.eventbus.core.repository.PublishStatus;

/**
 * @author ahoo wang
 */
public class PublishEventEntity extends PublishEvent<String> {
    private PublishStatus status;
    private Integer version;
    private Long publishedTime;


    public PublishStatus getStatus() {
        return status;
    }

    public void setStatus(PublishStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Long publishedTime) {
        this.publishedTime = publishedTime;
    }
}
