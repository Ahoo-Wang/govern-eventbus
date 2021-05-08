package me.ahoo.eventbus.core.publisher;


import me.ahoo.eventbus.core.repository.Identity;

/**
 * @author ahoo wang
 */
public class PublishEvent<TEventData> implements Identity {
    private Long id;
    private String eventName;
    private TEventData eventData;
    private Long createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public TEventData getEventData() {
        return eventData;
    }

    public void setEventData(TEventData eventData) {
        this.eventData = eventData;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}

