package me.ahoo.eventbus.core.compensate.db.config;

import me.ahoo.eventbus.core.compensate.CompensateConfig;
import me.ahoo.eventbus.core.compensate.ScheduleConfig;

/**
 * @author ahoo wang
 */
public class PublishConfig extends CompensateConfig {

    private LeaderConfig leader;

    private ScheduleConfig schedule;

    public PublishConfig() {
        schedule = ScheduleConfig.DEFAULT;
        leader = new LeaderConfig();
    }

    public LeaderConfig getLeader() {
        return leader;
    }

    public void setLeader(LeaderConfig leader) {
        this.leader = leader;
    }

    public ScheduleConfig getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleConfig schedule) {
        this.schedule = schedule;
    }
}
