package me.ahoo.eventbus.core.compensate;

import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
public class ScheduleConfig {
    public final static ScheduleConfig DEFAULT;
    /**
     * {@link TimeUnit#SECONDS}
     */
    private long initialDelay = TimeUnit.MINUTES.toSeconds(5);
    /**
     * {@link TimeUnit#SECONDS}
     */
    private long period = TimeUnit.MINUTES.toSeconds(5);

    static {
        DEFAULT = new ScheduleConfig();
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }
}
