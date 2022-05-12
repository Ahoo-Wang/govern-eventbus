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

import me.ahoo.simba.core.MutexContendServiceFactory;
import me.ahoo.simba.schedule.AbstractScheduler;
import me.ahoo.simba.schedule.ScheduleConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * AbstractCompensateScheduler.
 *
 * @author ahoo wang
 */
@Slf4j
public abstract class AbstractCompensateScheduler extends AbstractScheduler implements EventCompensate {
    
    
    public AbstractCompensateScheduler(String mutex,
                                       ScheduleConfig scheduleConfig,
                                       MutexContendServiceFactory contendServiceFactory) {
        super(mutex, scheduleConfig, contendServiceFactory);
    }
    
    protected abstract String getWorker();
    
    protected abstract void work();
    
}
