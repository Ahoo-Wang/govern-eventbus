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
import lombok.var;
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
        var protectedLeaderId = UUID.randomUUID().toString();
        try {
            var localHost = InetAddress.getLocalHost();
            return Strings.lenientFormat("%s@%s", protectedLeaderId, localHost.getHostAddress());
        } catch (UnknownHostException unknownHostException) {
            log.warn(unknownHostException.getMessage(), unknownHostException);
        }
        return protectedLeaderId;
    }

    /**
     * * 服务实例争抢领导权
     * * 1. 获取当前领导者 {@link CompensateLeader}
     * * 2. 判断 是否当前存在领导者 ({@link CompensateLeader#getTransitionPeriod()} VS {@link CompensateLeader#getCurrentTs()})
     * * 2.1 不存在领导者：乐观锁（通过 {@link CompensateLeader#getVersion()}）争抢领导者，更新 {@link CompensateLeader}
     * * 2.1.1 判断是否更新成功（通过更新返回值affected_rows=1）,如果是，则获得领导资格，执行领导任务
     * * 2.1.2 如果否，则争抢失败，等待下次争抢。
     * * 2.2 存在领导者：判断 {@link CompensateLeader#getLeaderId()} 是否为自己
     * * 2.2.1 如果不是，等待下次争抢
     * * 2.2.2 如果是 续期领导
     *
     * @param leaderRepository
     * @param leaderId
     * @return
     */
    public static boolean fightLeadership(LeaderRepository leaderRepository, String leaderId, LeaderConfig leaderConfig) {
        var currentLeader = leaderRepository.getLeader();
        if (log.isInfoEnabled()) {
            log.info("fightLeadership - fightLeaderId:[{}] currentLeader :[{}]", leaderId, currentLeader);
        }

        if (!currentLeader.hasLeader()) {
            return leaderRepository.fightLeadership(leaderConfig.getTermLength(), leaderConfig.getTransitionLength(), leaderId, currentLeader.getVersion());
        }

        if (!currentLeader.isInOfficeOf(leaderId)) {
            if (log.isInfoEnabled()) {
                log.info("fightLeadership - fightLeaderId:[{}] currentLeader  :[{}] is not me.", leaderId, currentLeader);
            }
            return false;
        }
        if (log.isInfoEnabled()) {
            log.info("fightLeadership - fightLeaderId:[{}] currentLeader  :[{}] is me.", leaderId, currentLeader);
        }

        /**
         * 续期
         */
        return leaderRepository.fightLeadership(leaderConfig.getTermLength(), leaderConfig.getTransitionLength(), leaderId, currentLeader.getVersion());
    }
}
