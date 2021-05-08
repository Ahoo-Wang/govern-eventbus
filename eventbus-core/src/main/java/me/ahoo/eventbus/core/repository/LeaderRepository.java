package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.repository.entity.CompensateLeader;

/**
 * @author ahoo wang
 */
public interface LeaderRepository {

    /**
     * get current Leader
     *
     * @return
     */
    CompensateLeader getLeader();

    /**
     * Fighting for leadership rights
     *
     * @param termLength       任期时长 {@link java.util.concurrent.TimeUnit#SECONDS}
     * @param transitionLength 过度期时长  {@link java.util.concurrent.TimeUnit#SECONDS}
     * @param leaderId
     * @param lastVersion
     * @return 1: success 2: failure
     */
    boolean fightLeadership(long termLength, long transitionLength, String leaderId, int lastVersion);

    /**
     * Release the leadership
     *
     * @param leaderId
     * @return
     */
    boolean releaseLeadership(String leaderId);
}
