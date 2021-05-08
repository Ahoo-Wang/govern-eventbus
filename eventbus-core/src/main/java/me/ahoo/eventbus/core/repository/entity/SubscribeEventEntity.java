package me.ahoo.eventbus.core.repository.entity;

import me.ahoo.eventbus.core.repository.SubscribeIdentity;

/**
 * @author ahoo wang
 */
public class SubscribeEventEntity extends SubscribeIdentity {
    private Long subscribeTime;
    private Long eventId;
    private String eventName;
    private String eventData;
    private Long eventCreateTime;
    private Long createTime;

    public Long getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(Long subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public Long getEventCreateTime() {
        return eventCreateTime;
    }

    public void setEventCreateTime(Long eventCreateTime) {
        this.eventCreateTime = eventCreateTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
