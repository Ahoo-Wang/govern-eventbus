package me.ahoo.eventbus.demo.event;

import me.ahoo.eventbus.core.annotation.Event;

/**
 * @author ahoo wang
 * createTime 2020/2/17 20:12
 */
@Event
public class PublishDataEvent {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
