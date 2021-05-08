package me.ahoo.eventbus.core.compensate.db;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.compensate.AbstractSubscribeCompensate;
import me.ahoo.eventbus.core.compensate.db.config.SubscribeConfig;
import me.ahoo.eventbus.core.repository.SubscribeEventRepository;
import me.ahoo.eventbus.core.serialize.Deserializer;
import me.ahoo.eventbus.core.subscriber.SubscriberRegistry;
import me.ahoo.eventbus.core.utils.Threads;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@Slf4j
public class DbSubscribeCompensate extends AbstractSubscribeCompensate {

    private final SubscribeConfig subscribeConfig;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final String leaderId;

    public DbSubscribeCompensate(
            Deserializer deserializer,
            SubscribeConfig subscribeConfig,
            SubscriberRegistry subscriberRegistry,
            SubscribeEventRepository subscribeEventRepository) {
        super(subscribeConfig, deserializer, subscriberRegistry, subscribeEventRepository);
        this.subscribeConfig = subscribeConfig;
        this.leaderId = CompensateLeaderService.generateLeaderId();
    }

    @Override
    protected void start0() {
        if (Objects.isNull(this.scheduledThreadPoolExecutor)) {
            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, Threads.defaultFactory("ShardingSubscribeCompensate"));
        }
        var scheduleConfig = subscribeConfig.getSchedule();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::doWork, scheduleConfig.getInitialDelay(), scheduleConfig.getPeriod(), TimeUnit.SECONDS);
    }

    private void doWork() {
        if (!CompensateLeaderService.fightLeadership(subscribeEventRepository, leaderId, subscribeConfig.getLeader())) {
            return;
        }
        schedule();
    }

    @Override
    protected void stop0() {
        if (Objects.nonNull(scheduledThreadPoolExecutor)) {
            scheduledThreadPoolExecutor.shutdown();
        }
        boolean succeeded = subscribeEventRepository.releaseLeadership(leaderId);
        if (log.isInfoEnabled()) {
            if (succeeded) {
                log.info("stop0 - Release leadership successfully, leaderId:[{}].", leaderId);
                return;
            }
            log.info("stop0 - Failed to release leadership, because I'm not a leader.leaderId:[{}].", leaderId);
        }
    }

}
