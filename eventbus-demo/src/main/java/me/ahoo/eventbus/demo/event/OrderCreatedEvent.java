package me.ahoo.eventbus.demo.event;

/**
 * @author ahoo wang
 */
public class OrderCreatedEvent {
    private long orderId;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                '}';
    }
}
