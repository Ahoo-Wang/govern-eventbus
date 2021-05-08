package me.ahoo.eventbus.core.consistency;

import me.ahoo.eventbus.core.repository.PublishIdentity;

import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * execution flow
 * 1. begin local transaction
 * 2. invoke local biz code
 * 3. insert publish event:INITIALIZED to local db
 * 4. commit local transaction
 * --- try
 * 5. publish event to event-bus(MQ)
 * 6. update publish event status to SUCCEEDED
 * --- catch update publish event to FAILED
 *
 * @author ahoo wang
 */
public interface ConsistencyPublisher {

    Object publish(Supplier<Object> publishDataSupplier);

    /**
     * publish event to bus
     *
     * @param publishIdentity  publish event id
     * @param publishEventData publish event data
     * @return
     */
    Future<?> publish(PublishIdentity publishIdentity, Object publishEventData);
}
