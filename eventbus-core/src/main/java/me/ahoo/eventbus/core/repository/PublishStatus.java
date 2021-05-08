package me.ahoo.eventbus.core.repository;

/**
 * 发布事件状态
 *
 * @author ahoo wang
 */
public enum PublishStatus {

    INITIALIZED(0),
    SUCCEEDED(1),
    FAILED(2);
    private int value;

    PublishStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PublishStatus valueOf(int value) {
        switch (value) {
            case 0:
                return INITIALIZED;
            case 1:
                return SUCCEEDED;
            case 2:
                return FAILED;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }
}
