package me.ahoo.eventbus.core.repository;

/**
 * 订阅状态
 *
 * @author ahoo wang
 */
public enum SubscribeStatus {

    INITIALIZED(0),
    SUCCEEDED(1),
    FAILED(2);
    private int value;

    SubscribeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SubscribeStatus valeOf(int value) {

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
