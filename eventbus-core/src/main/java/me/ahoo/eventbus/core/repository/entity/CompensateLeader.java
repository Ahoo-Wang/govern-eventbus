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

package me.ahoo.eventbus.core.repository.entity;

import me.ahoo.eventbus.core.repository.Version;

/**
 * 事件补偿领导者/协调者
 *
 * @author ahoo wang
 */
public class CompensateLeader implements Version {
    public static final String PUBLISH_LEADER = "publish_leader";
    public static final String SUBSCRIBE_LEADER = "subscribe_leader";
    /**
     * publish_leader or subscribe_leader
     */
    private String name;
    /**
     * 任期开始时间
     * {@link java.util.concurrent.TimeUnit#SECONDS}
     */
    private long termStart;
    /**
     * 任期结束时间
     * 用于领导者检查是否到达任期，到达任期后不再执行领导者任务
     * {@link java.util.concurrent.TimeUnit#SECONDS}
     */
    private long termEnd;
    /**
     * 缓冲期/过渡期
     * 用于缓冲领导者任务执行时间
     * {@link java.util.concurrent.TimeUnit#SECONDS}
     */
    private long transitionPeriod;
    /**
     * 用于检查自己是否是领导者
     * 领导者编号
     */
    private String leaderId;
    /**
     * 版本号，用于领导者并发争抢控制
     */
    private Integer version;

    /**
     * 当前时间戳 (统一使用Db时间作为统一时间，防止全局时间不一致)
     */
    private long currentTs;

    /**
     * 判断 是否当前存在领导者 ( {@link CompensateLeader#transitionPeriod} VS {@link CompensateLeader#currentTs})
     *
     * @return
     */
    public boolean hasLeader() {
        return transitionPeriod > currentTs;
    }

    /**
     * 判断当前领导者是否是我
     *
     * @param leaderId
     * @return
     */
    public boolean isLeader(String leaderId) {
        return this.leaderId.equals(leaderId);
    }

    /**
     * 判断是否在任期内
     *
     * @return
     */
    public boolean isInOffice() {
        return this.termEnd > currentTs;
    }

    public boolean isInOfficeOf(String leaderId) {
        return isLeader(leaderId)
                && isInOffice();
    }

    public String getName() {
        return this.name;
    }

    public long getTermStart() {
        return this.termStart;
    }

    public long getTermEnd() {
        return this.termEnd;
    }

    public long getTransitionPeriod() {
        return this.transitionPeriod;
    }

    public String getLeaderId() {
        return this.leaderId;
    }

    public Integer getVersion() {
        return this.version;
    }

    public long getCurrentTs() {
        return this.currentTs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTermStart(long termStart) {
        this.termStart = termStart;
    }

    public void setTermEnd(long termEnd) {
        this.termEnd = termEnd;
    }

    public void setTransitionPeriod(long transitionPeriod) {
        this.transitionPeriod = transitionPeriod;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setCurrentTs(long currentTs) {
        this.currentTs = currentTs;
    }

    @Override
    public String toString() {
        return "CompensateLeader{" +
                "name='" + name + '\'' +
                ", termStart=" + termStart +
                ", termEnd=" + termEnd +
                ", transitionPeriod=" + transitionPeriod +
                ", leaderId='" + leaderId + '\'' +
                ", version=" + version +
                ", currentTs=" + currentTs +
                '}';
    }
}
