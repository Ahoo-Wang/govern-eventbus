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

package me.ahoo.eventbus.core;

import lombok.SneakyThrows;
import me.ahoo.eventbus.core.utils.Threads;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author ahoo wang
 */
public class ScheduledThreadPoolExecutorTests {
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public ScheduledThreadPoolExecutorTests() {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2, Threads.defaultFactory("SubscribeCompensation"));
    }

    @SneakyThrows
    @Test
    public void schedule() {
        LongAdder longAdder = new LongAdder();
        AtomicInteger atomicInteger = new AtomicInteger();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            try {
                longAdder.increment();

                System.out.println("run-" + Thread.currentThread().getName() + "-" + longAdder.intValue());
                TimeUnit.SECONDS.sleep(2);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            try {
                longAdder.increment();
                System.out.println("run-" + Thread.currentThread().getName() + "-" + longAdder.intValue());
                TimeUnit.SECONDS.sleep(2);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(20);
    }
}
