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

package me.ahoo.eventbus.zookeeper;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.compensate.ScheduleConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author ahoo wang
 */
@Slf4j
public class CompensateLeaderSelectorListener extends LeaderSelectorListenerAdapter {

    private final String leaderPath;
    private final Consumer<Object> doWork;
    private final ScheduleConfig scheduleConfig;

    public CompensateLeaderSelectorListener(String leaderPath,
                                            Consumer<Object> doWork,
                                            ScheduleConfig scheduleConfig) {
        this.leaderPath = leaderPath;
        this.doWork = doWork;
        this.scheduleConfig = scheduleConfig;
    }

    /**
     * Called when your instance has been granted leadership. This method
     * should not return until you wish to release leadership
     *
     * @param client the client
     * @throws Exception any errors
     */
    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        try {
            if (log.isInfoEnabled()) {
                log.info("takeLeadership - [{}].", leaderPath);
            }
            /**
             * InitialDelay
             */
            if (log.isInfoEnabled()) {
                log.info("takeLeadership - [{}] InitialDelay [{}]s", leaderPath, scheduleConfig.getInitialDelay());
            }
            TimeUnit.SECONDS.sleep(scheduleConfig.getInitialDelay());
            while (!Thread.currentThread().isInterrupted()) {
                /**
                 * doWork
                 */
                doWork.accept(client);
                /**
                 * Period
                 */
                if (log.isInfoEnabled()) {
                    log.info("takeLeadership - [{}] Period [{}]s", leaderPath, scheduleConfig.getPeriod());
                }
                TimeUnit.SECONDS.sleep(scheduleConfig.getPeriod());
            }
        } catch (InterruptedException interruptedException) {
            if (log.isInfoEnabled()) {
                log.warn(interruptedException.getMessage(), interruptedException);
            }
            Thread.currentThread().interrupt();
        } finally {
            if (log.isInfoEnabled()) {
                log.info("takeLeadership - [{}] relinquishing leadership.", leaderPath);
            }
        }
    }
}
