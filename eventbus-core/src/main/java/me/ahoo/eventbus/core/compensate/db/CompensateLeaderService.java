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

package me.ahoo.eventbus.core.compensate.db;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.compensate.db.config.LeaderConfig;
import me.ahoo.eventbus.core.repository.LeaderRepository;
import me.ahoo.eventbus.core.repository.entity.CompensateLeader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author ahoo wang
 */
@Slf4j
public final class CompensateLeaderService {

    public static String generateLeaderId() {
        String protectedLeaderId = UUID.randomUUID().toString();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return Strings.lenientFormat("%s@%s", protectedLeaderId, localHost.getHostAddress());
        } catch (UnknownHostException unknownHostException) {
            log.warn(unknownHostException.getMessage(), unknownHostException);
        }
        return protectedLeaderId;
    }

    /**
     * 服务实例竞争领导权
     * <p>1. 获取当前领导者 {@link CompensateLeader}
     * <p>2. 判断 是否当前存在领导者 ({@link CompensateLeader#getTransitionPeriod()} VS {@link CompensateLeader#getCurrentTs()})
     * <p> 2.1 不存在领导者，乐观锁（通过 {@link CompensateLeader#getVersion()}）竞争领导者，更新 {@link CompensateLeader}
     * <p>  2.1.1 判断是否更新成功（通过更新返回值affected_rows=1）,如果是，则获得领导资格，执行领导任务
     * <p>  2.1.2 如果否，则争抢失败，等待下次争抢。
     * <p> 2.2 存在领导者，判断 {@link CompensateLeader#isInTransition()} 领导者任期不在过渡期内?
     * <p>  2.2.1 领导者任期不在过渡期内，竞争领导者
     * <p>  2.2.2 存在领导者 & [在任期内]，判断领导者是否为自己{@link CompensateLeader#isLeader(String)}
     * <p>   2.2.2.1 当前领导者不是我自己，退出竞争
     * <p>   2.2.2.2 当前领导者是我自己，判断自己是否在任期内{@link CompensateLeader#isInOffice()}
     * <p>    2.2.2.2.1 在[任期]内，不需要申请连任
     * <p>    2.2.2.2.2 不在[任期]内，申请连任
     *
     * @param leaderRepository
     * @param leaderId         候选领导者Id
     * @return true:成功竞争领导者 or 已领导者
     */
    public static FightLeadershipResult fightLeadership(LeaderRepository leaderRepository, String leaderId, LeaderConfig leaderConfig) {
        CompensateLeader currentLeader = leaderRepository.getLeader();
        if (!currentLeader.hasLeader()) {
            /**
             * [当前不存在领导者]，竞争领导权
             */
            boolean succeeded = leaderRepository.fightLeadership(leaderConfig.getTermLength(), leaderConfig.getTransitionLength(), leaderId, currentLeader.getVersion());
            if (log.isInfoEnabled()) {
                log.info("fightLeadership - fightLeaderId:[{}] - Currently there is no leader:[{}], compete for leadership - succeeded:[{}].", leaderId, currentLeader, succeeded);
            }
            return new FightLeadershipResult(succeeded, leaderId, currentLeader);
        }
        /**
         * [存在领导者]
         */
        if (!currentLeader.isInTransition()) {
            /**
             * 当前存在领导者,但是[领导者任期不在过渡期内]，竞争领导者
             */
            boolean succeeded = leaderRepository.fightLeadership(leaderConfig.getTermLength(), leaderConfig.getTransitionLength(), leaderId, currentLeader.getVersion());
            if (log.isInfoEnabled()) {
                log.info("fightLeadership - fightLeaderId:[{}] - Currently there is leader:[{}], but the leader’s term is not in the transition period, compete for leadership - succeeded:[{}].", leaderId, currentLeader, succeeded);
            }
            return new FightLeadershipResult(succeeded, leaderId, currentLeader);
        }
        /**
         * 存在领导者 & [在任期内]
         */
        if (!currentLeader.isLeader(leaderId)) {
            /**
             * 当前存在领导者,并且在在过渡期内，[但是当前领导者不是我自己]，退出竞争
             */
            if (log.isInfoEnabled()) {
                log.info("fightLeadership - fightLeaderId:[{}] - Currently there is leader:[{}], and it is in the transition period, but the current leader is not myself, so I withdraw from the competition.", leaderId, currentLeader);
            }
            return new FightLeadershipResult(false, leaderId, currentLeader);
        }
        /**
         * 当前存在领导者 & 在过渡期内 & [当前领导者是我自己]
         */
        if (currentLeader.isInOffice()) {
            /**
             * 当前存在领导者 & 在过渡期内 & 当前领导者是我自己 & 在[任期]内，不需要申请续约/连任
             */
            if (log.isInfoEnabled()) {
                log.info("fightLeadership - fightLeaderId:[{}]  - Currently there is leader:[{}], and it is in the office, and the current leader is myself, no need to apply for renewal.", leaderId, currentLeader);
            }
            return new FightLeadershipResult(true, leaderId, currentLeader);
        }
        /**
         * 当前存在领导者 & 在过渡期内 & 当前领导者是我自己 & 不在[任期]内 - 申请续约/连任
         */
        boolean succeeded = leaderRepository.renewLeadership(leaderConfig.getTermLength(), leaderConfig.getTransitionLength(), leaderId, currentLeader.getVersion());
        if (log.isInfoEnabled()) {
            log.info("fightLeadership - fightLeaderId:[{}]  - Currently there is leader:[{}], and it is in the transition period, and the current leader is myself, so apply for renewal - succeeded:[{}].", leaderId, currentLeader, succeeded);
        }
        return new FightLeadershipResult(succeeded, leaderId, currentLeader);
    }


    public static class FightLeadershipResult {
        private final boolean succeeded;
        private final String candidateLeaderId;
        private final CompensateLeader beforeLeader;

        public FightLeadershipResult(boolean succeeded, String candidateLeaderId, CompensateLeader beforeLeader) {
            this.succeeded = succeeded;
            this.candidateLeaderId = candidateLeaderId;
            this.beforeLeader = beforeLeader;
        }

        public boolean isSucceeded() {
            return succeeded;
        }

        public String getCandidateLeaderId() {
            return candidateLeaderId;
        }

        public CompensateLeader getBeforeLeader() {
            return beforeLeader;
        }
    }
}
