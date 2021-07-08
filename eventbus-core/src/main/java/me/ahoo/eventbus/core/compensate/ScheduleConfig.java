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
