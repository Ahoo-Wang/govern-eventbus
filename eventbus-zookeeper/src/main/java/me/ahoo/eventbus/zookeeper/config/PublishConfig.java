package me.ahoo.eventbus.zookeeper.config;

import me.ahoo.eventbus.core.compensate.CompensateConfig;
import me.ahoo.eventbus.core.compensate.ScheduleConfig;

/**
 * @author ahoo wang
 */
public class PublishConfig extends CompensateConfig {
    private String leaderPrefix;
    private ScheduleConfig schedule;

    public PublishConfig() {
        schedule = ScheduleConfig.DEFAULT;
    }

    public String getLeaderPrefix() {
        return leaderPrefix;
    }

    public void setLeaderPrefix(String leaderPrefix) {
        this.leaderPrefix = leaderPrefix;
    }

    public ScheduleConfig getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleConfig schedule) {
        this.schedule = schedule;
    }
}
