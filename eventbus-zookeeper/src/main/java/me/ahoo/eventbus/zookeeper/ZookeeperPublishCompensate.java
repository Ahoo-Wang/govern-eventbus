package me.ahoo.eventbus.zookeeper;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.eventbus.core.compensate.AbstractPublishCompensate;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;
import me.ahoo.eventbus.core.repository.PublishEventRepository;
import me.ahoo.eventbus.zookeeper.config.PublishConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;

import java.util.Objects;

/**
 * @author ahoo wang
 */
@Slf4j
public class ZookeeperPublishCompensate extends AbstractPublishCompensate {

    private static final String LEADER = "publish_leader";
    /**
     * lockPath: /eventbus/{service_name}/publish
     */
    private final String LEADER_PATH;
    private final CuratorFramework zookeeperClient;
    private final PublishConfig publishConfig;
    private LeaderSelector leaderSelector;

    public ZookeeperPublishCompensate(
            PublishConfig publishConfig,
            CuratorFramework zookeeperClient,
            ConsistencyPublisher consistencyPublisher,
            PublishEventRepository publishEventRepository) {
        super(publishConfig, consistencyPublisher, publishEventRepository);
        this.publishConfig = publishConfig;
        this.zookeeperClient = zookeeperClient;
        this.LEADER_PATH = getLeaderPath();
    }

    private String getLeaderPath() {
        return Strings.lenientFormat("%s/%s", publishConfig.getLeaderPrefix(), LEADER);
    }

    @Override
    public void start0() {
        var listener = new CompensateLeaderSelectorListener(LEADER_PATH, o -> schedule(), publishConfig.getSchedule());
        leaderSelector = new LeaderSelector(zookeeperClient, LEADER_PATH, listener);
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }

    @SneakyThrows
    @Override
    public void stop0() {
        if (Objects.nonNull(leaderSelector)) {
            leaderSelector.close();
        }
    }
}
