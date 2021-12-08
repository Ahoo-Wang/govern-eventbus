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

import java.time.Duration;

/**
 * @author ahoo wang
 */
public class CompensateConfig {

    private Integer maxVersion = 10;
    private Integer batch = 10;
    private Duration before = Duration.ofMinutes(1);
    private Duration range = Duration.ofDays(7);

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

    public Duration getRange() {
        return range;
    }

    public void setRange(Duration range) {
        this.range = range;
    }

    public Duration getBefore() {
        return before;
    }

    public void setBefore(Duration before) {
        this.before = before;
    }
}
