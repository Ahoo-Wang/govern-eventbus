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
