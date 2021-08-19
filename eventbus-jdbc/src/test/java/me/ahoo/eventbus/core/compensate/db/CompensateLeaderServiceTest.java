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

import com.mysql.cj.jdbc.MysqlDataSource;
import me.ahoo.eventbus.core.compensate.db.config.LeaderConfig;
import me.ahoo.eventbus.core.repository.LeaderRepository;
import me.ahoo.eventbus.jdbc.JdbcPublishEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

/**
 * @author ahoo wang
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompensateLeaderServiceTest {

    LeaderRepository leaderRepository;

    @BeforeAll
    public void setup() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/eventbus_db?serverTimezone=GMT%2B8&characterEncoding=utf-8");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        leaderRepository = new JdbcPublishEventRepository(null, namedParameterJdbcTemplate);
    }

    @Test
    void fightLeadership() {
        LeaderConfig leaderConfig = new LeaderConfig();
        leaderConfig.setTermLength(1);
        leaderConfig.setTransitionLength(1);
        String leaderId1 = UUID.randomUUID().toString();
        String leaderId2 = UUID.randomUUID().toString();
        boolean succeeded = CompensateLeaderService.fightLeadership(leaderRepository, leaderId1, leaderConfig).isSucceeded();
        Assertions.assertTrue(succeeded);
        succeeded = CompensateLeaderService.fightLeadership(leaderRepository, leaderId2, leaderConfig).isSucceeded();
        Assertions.assertFalse(succeeded);
        succeeded = leaderRepository.releaseLeadership(leaderId1);
        Assertions.assertTrue(succeeded);
    }

    @Test
    void fightLeadershipConcurrent() {
        LeaderConfig leaderConfig = new LeaderConfig();
        leaderConfig.setTermLength(1);
        leaderConfig.setTransitionLength(5);
        String[] leaderIds = new String[100];
        for (int i = 0; i < 100; i++) {
            leaderIds[i] = UUID.randomUUID().toString();
        }

        CompletableFuture[] futures = new CompletableFuture[300];
        for (int i = 0; i < 100; i++) {
            String leaderId = leaderIds[i];
            futures[i] = CompletableFuture.supplyAsync(() -> CompensateLeaderService.fightLeadership(leaderRepository, leaderId, leaderConfig));
        }
        for (int i = 0; i < 100; i++) {
            String leaderId = leaderIds[i];
            futures[100 + i] = CompletableFuture.supplyAsync(() -> CompensateLeaderService.fightLeadership(leaderRepository, leaderId, leaderConfig));
        }
        LockSupport.parkNanos(Duration.ofSeconds(2).toNanos());
        for (int i = 0; i < 100; i++) {
            String leaderId = leaderIds[i];
            futures[200 + i] = CompletableFuture.supplyAsync(() -> CompensateLeaderService.fightLeadership(leaderRepository, leaderId, leaderConfig));
        }
        CompletableFuture.allOf(futures).join();
    }
}
