package network;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public final class ClusterManager {
    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String PREFIX = "/PastelCoin";

    private final ZooKeeper zooKeeper;
    private String currentZnode;

    public ClusterManager() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, null);
    }

    public void registerNode(final String address) throws Exception {
        if (currentZnode != null) {
            return;
        }

        try {
            final String path = PREFIX + "/" + address;
            final String znode = this.zooKeeper.create(
                    path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            this.currentZnode = znode;

            logger.info(String.format("znode %s is registered", znode));
        } catch (final KeeperException | InterruptedException e) {
            throw new Exception(e);
        }
    }

    public List<String> getNodes() throws Exception {
        try {
            return this.zooKeeper.getChildren(PREFIX, false);
        } catch (KeeperException | InterruptedException e) {
            throw new Exception(e);
        }
    }
}
