package me.ahoo.eventbus.core.consistency;

import me.ahoo.eventbus.core.subscriber.Subscriber;

/**
 * execution flow
 * getSubscribeEventBy event_id and event_name and subscribe_name
 * if exist
 * status is SUCCEEDED throw RepeatedSubscribeException
 * status is INITIALIZED
 * else insert subscribe event:INITIALIZED to local db
 * ---- try
 * 1. begin local transaction
 * 2. update subscribe event to SUCCEEDED (Optimistic lock update)
 * 3. invoke local biz code
 * 4. commit local transaction
 * ---- catch update subscribe event to FAILED
 *
 * @author ahoo wang
 */
public interface ConsistencySubscriber extends Subscriber {

    Subscriber getTargetSubscriber();

}
