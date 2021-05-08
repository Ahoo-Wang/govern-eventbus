package me.ahoo.eventbus.demo.event;

import me.ahoo.eventbus.core.annotation.FieldEvent;

/**
 * @author ahoo wang
 * createTime 2020/3/30 21:35
 */
public class FieldEventWrapper {
    private String resp;
    @FieldEvent
    private FieldEventData fieldEventData;

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }

    public FieldEventData getFieldEventData() {
        return fieldEventData;
    }

    public void setFieldEventData(FieldEventData fieldEventData) {
        this.fieldEventData = fieldEventData;
    }
}
