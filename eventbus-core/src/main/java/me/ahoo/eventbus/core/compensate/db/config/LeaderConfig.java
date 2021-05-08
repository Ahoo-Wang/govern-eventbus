package me.ahoo.eventbus.core.compensate.db.config;

import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
public class LeaderConfig {
    private long termLength = TimeUnit.MINUTES.toSeconds(5);
    private long transitionLength = TimeUnit.MINUTES.toSeconds(1);

    public long getTermLength() {
        return termLength;
    }

    public void setTermLength(long termLength) {
        this.termLength = termLength;
    }

    public long getTransitionLength() {
        return transitionLength;
    }

    public void setTransitionLength(long transitionLength) {
        this.transitionLength = transitionLength;
    }
}
