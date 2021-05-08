package me.ahoo.eventbus.core.compensate;

import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
public class CompensateConfig {
    private Integer maxVersion = 10;
    private Integer batch = 10;
    private long before = TimeUnit.MINUTES.toMillis(5);

    public Integer getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(Integer maxVersion) {
        this.maxVersion = maxVersion;
    }

    public Integer getBatch() {
        return batch;
    }

    public void setBatch(Integer batch) {
        this.batch = batch;
    }

    public long getBefore() {
        return before;
    }

    public void setBefore(long before) {
        this.before = before;
    }
}
