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

package me.ahoo.eventbus.core.repository;

import me.ahoo.eventbus.core.repository.entity.CompensateLeader;

/**
 * @author ahoo wang
 */
public interface LeaderRepository {

    /**
     * get current Leader
     *
     * @return
     */
    CompensateLeader getLeader();

    /**
     * Fighting for leadership rights
     *
     * @param termLength       任期时长 {@link java.util.concurrent.TimeUnit#SECONDS}
     * @param transitionLength 过度期时长  {@link java.util.concurrent.TimeUnit#SECONDS}
     * @param leaderId
     * @param lastVersion
     * @return 1: success 2: failure
     */
    boolean fightLeadership(long termLength, long transitionLength, String leaderId, int lastVersion);

    /**
     * 申请续约
     * @param termLength
     * @param transitionLength
     * @param leaderId
     * @param lastVersion
     * @return
     */
    boolean renewLeadership(long termLength, long transitionLength, String leaderId, int lastVersion);

    /**
     * Release the leadership
     *
     * @param leaderId
     * @return
     */
    boolean releaseLeadership(String leaderId);
}
