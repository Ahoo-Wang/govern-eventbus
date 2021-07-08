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
