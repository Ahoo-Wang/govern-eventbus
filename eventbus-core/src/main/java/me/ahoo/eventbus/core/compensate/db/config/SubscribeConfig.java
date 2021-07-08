/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.eventbus.core.compensate.db.config;

import me.ahoo.eventbus.core.compensate.CompensateConfig;
import me.ahoo.eventbus.core.compensate.ScheduleConfig;


/**
 * @author ahoo wang
 */
public class SubscribeConfig extends CompensateConfig {
    private LeaderConfig leader;
    private ScheduleConfig schedule;

    public SubscribeConfig() {
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
