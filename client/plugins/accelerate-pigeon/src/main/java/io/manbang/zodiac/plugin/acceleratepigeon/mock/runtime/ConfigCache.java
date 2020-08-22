package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;



import com.dianping.lion.Constants;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.lion.util.Utils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dianping.lion.util.Utils.trimToNull;

/**
 * @author xuxiaolu
 */
public enum ConfigCache {
    /**
     * 实例
     */
    INSTANCE,;

    private CuratorClient curatorClient;

    public void destroy() {
        if (this.curatorClient != null) {
            this.curatorClient.close();
        }
    }

    ConfigCache() {
        this.curatorClient = new CuratorClient(EnvZooKeeperConfig.getZKAddress());
    }

    public static ConfigCache getInstance() {
        return INSTANCE;
    }

    public static ConfigCache getInstance(String address) {
        return INSTANCE;
    }

    public String getProperty(String key) throws LionException {
        key = trimToNull(key);
        if (key == null || key.length() == 0) {
            throw new LionException("key is null");
        }
        return this.curatorClient.getZkValue(key);
    }

    public Long getLongProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    public Integer getIntProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public Short getShortProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Short.parseShort(value);
    }

    public Byte getByteProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Byte.parseByte(value);
    }

    public Float getFloatProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Float.parseFloat(value);
    }

    public Double getDoubleProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    public Boolean getBooleanProperty(String key) throws LionException {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public void addChange(ConfigChange change) {
        this.curatorClient.changeList.add(change);
    }

    public void removeChange(ConfigChange change) {
        this.curatorClient.changeList.remove(change);
    }

    private static class CuratorClient {

        private static final int DEFAULT_SYNC_INTERVAL = 120000;

        private static final String MAGIC_VALUE = "~!@#$%^&*()_+";

        private static final Object LOCK = new Object();

        private volatile boolean isStop = false;

        private Logger logger = LoggerFactory.getLogger(CuratorClient.class);

        private CuratorFramework curatorFramework;

        private volatile boolean isConnected = false;

        private Map<String, String> configCache = new ConcurrentHashMap<>();

        private Map<String, Long> timestampCache = new ConcurrentHashMap<>();

        private List<ConfigChange> changeList = new CopyOnWriteArrayList<>();

        private ConcurrentHashMap<String, Object> keyLocks = new ConcurrentHashMap<>();

        private CuratorClient(String address) {
            logger.info(">>>>>>>> lion zookeeper address: {}", address);
            this.curatorFramework = CuratorFrameworkFactory.newClient(address, 60 * 1000, 30 * 1000,
                    new RetryNTimes(Integer.MAX_VALUE, 1000));

            this.curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    logger.info(">>>>>>>> lion zookeeper state changed to {}", newState);
                    if (newState == ConnectionState.CONNECTED) {
                        isConnected = true;
                    } else if (newState == ConnectionState.RECONNECTED) {
                        isConnected = true;
                        try {
                            logger.info(">>>>>>>> lion zookeeper state changed to {}, so sync config", newState);
                            syncConfig();
                        } catch (Exception e) {
                            logger.error(">>>>>>>> failed to watch all lion key", e);
                        }
                    } else {
                        isConnected = false;
                    }
                }
            });

            this.curatorFramework.getCuratorListenable().addListener(new CuratorClient.ConfigDataWatcher());

            this.curatorFramework.start();

            try {
                this.curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
            } catch (Exception ignore) {

            }

            startConfigSyncThread();
        }

        private void watch(String path) {
            if (isConnected) {
                try {
                    this.curatorFramework.checkExists().watched().forPath(path);
                } catch (Exception ignore) {
                }
            }
        }

        private void startConfigSyncThread() {
            Thread t = new Thread(new CuratorClient.ConfigDataSynchronization(), "lion-config-sync");
            t.setDaemon(true);
            t.start();
        }

        private String getZkValue(String key) throws LionException {
            String value = configCache.get(key);
            if (value == null) {
                try {
                    String group = getGroup();
                    if (group != null) {
                        // Try to get config for the group, if no value found, fall back to default
                        value = getValue(key, group);
                    }

                    // Group is null or no config for the group, fall back to default
                    if (value == null) {
                        value = getValue(key);
                    }
                } catch (Exception ex) {
                    logger.error("failed to get value for key: " + key, ex);
                    throw new LionException(ex);
                }
            }

            if (MAGIC_VALUE.equals(value)) {
                return null;
            }
            return value;
        }

        private boolean exists(String path) {
            if (isConnected) {
                try {
                    Stat stat = this.curatorFramework.checkExists().forPath(path);
                    return stat != null;
                } catch (Exception ignore) {
                }
            }
            return false;
        }

        private byte[] getDataWatched(String path) throws Exception {
            try {
                return curatorFramework.getData().watched().forPath(path);
            } catch (NoNodeException e) {
                curatorFramework.checkExists().watched().forPath(path);
                return null;
            }
        }

        private byte[] getData(String path) throws Exception {
            try {
                return curatorFramework.getData().forPath(path);
            } catch (NoNodeException e) {
                return null;
            }
        }

        private String getGroup() {
            String group = EnvZooKeeperConfig.getSwimlane();
            return trimToNull(group);
        }

        private String getConfigPath(String key) {
            return getConfigPath(key, null);
        }

        private String getConfigPath(String key, String group) {
            String path = Constants.CONFIG_PATH + Constants.PATH_SEPARATOR + key;
            if (group != null) {
                path = path + Constants.PATH_SEPARATOR + group;
            }
            return path;
        }

        private String getValue(String key) throws Exception {
            return getValue(key, null);
        }

        private String getValue(String key, String group) throws Exception {
            String path = getConfigPath(key, group);
            String timestampPath = getTimestampPath(path);
            String value = null;
            byte[] data = getDataWatched(path);
            if (data != null) {
                value = new String(data, Constants.CHARSET);
                // Cache key <-> value
                configCache.put(key, value);
                // Cache path <-> timestamp
                data = getData(timestampPath);
                if (data != null) {
                    Long timestamp = Utils.getLong(data);
                    timestampCache.put(path, timestamp);
                }
            } else {
                configCache.put(key, MAGIC_VALUE);
            }
            return value;
        }

        private void close() {
            if (isStop) {
                return;
            }
            isStop = true;
            configCache.clear();
            timestampCache.clear();
            changeList.clear();
            if (this.curatorFramework != null) {
                CloseableUtils.closeQuietly(this.curatorFramework);
            }
        }

        private String escape(String key, String value) {
            if (key == null) {
                return limitLength(value);
            }
            if (key.toLowerCase().contains("password")) {
                return "********";
            }
            return limitLength(value);
        }

        private String limitLength(String value) {
            if (value == null || value.length() <= 100) {
                return value;
            }
            return value.substring(0, 100);
        }

        private String getRealConfigPath(String key, String group) {
            String path = getConfigPath(key);
            if (group != null) {
                String path_ = getConfigPath(key, group);
                if (exists(path_)) {
                    path = path_;
                }
            }
            return path;
        }

        private void syncConfig() throws Exception {
            String group = getGroup();
            for (Entry<String, String> entry : configCache.entrySet()) {
                if (keyLocks.putIfAbsent(entry.getKey(), LOCK) == null) {
                    try {
                        String path = getRealConfigPath(entry.getKey(), group);
                        String timestampPath = getTimestampPath(path);

                        byte[] data = getData(timestampPath);
                        if (data != null) {
                            Long timestamp = Utils.getLong(data);
                            Long timestamp_ = timestampCache.get(path);
                            if (timestamp_ == null || timestamp > timestamp_) {
                                data = getDataWatched(path);
                                if (data != null) {
                                    timestampCache.put(path, timestamp);
                                    String value = new String(data, Constants.CHARSET);
                                    if (!value.equals(entry.getValue())) {
                                        entry.setValue(value);
                                        logger.info(">>>>>> lion config changed, key: {}, value: {}", entry.getKey(), escape(entry.getKey(), value));
                                        if (changeList != null) {
                                            for (ConfigChange change : changeList) {
                                                change.onChange(entry.getKey(), value);
                                            }
                                        }
                                    }
                                }
                            } else {
                                watch(path);
                            }
                        }
                    } finally {
                        keyLocks.remove(entry.getKey());
                    }
                }
            }
        }

        private String getTimestampPath(String path) {
            return path + Constants.PATH_SEPARATOR + Constants.CONFIG_TIMESTAMP;
        }

        private class ConfigDataWatcher implements CuratorListener {

            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                if (event.getType() == CuratorEventType.WATCHED) {
                    WatchedEvent we = event.getWatchedEvent();
                    if (we.getPath() != null) {
                        logger.info(">>>>>>> lion zookeeper event received, path: {}, event {}", we.getPath(), we.getType());
                        process(event.getWatchedEvent());
                    }
                }
            }

            public void process(WatchedEvent event) {
                String key = getKey(event.getPath());
                if (keyLocks.putIfAbsent(key, LOCK) == null) {
                    try {
                        if (event.getType() == EventType.NodeCreated || event.getType() == EventType.NodeDataChanged) {
                            String path = event.getPath();
                            String tsPath = getTimestampPath(path);
                            byte[] data = getData(tsPath);
                            if (data != null) {
                                Long timestamp = Utils.getLong(data);
                                Long timestamp_ = timestampCache.get(path);
                                if (timestamp_ == null || timestamp > timestamp_) {
                                    data = getDataWatched(path);
                                    if (data != null) {
                                        //1.watch比较快，创建节点时会触发两次watch，create和change，create读取到的值为byte[0], 需要过滤掉，让change事件来处理，上层会收到onChange;
                                        //2.watch比较慢，创建节点时会触发一次watch，create，create读取到的值为最新值，如果最新值(配置的值)是""，这时这一步过滤，上层会收不到onChange，但是考虑到影响面，这个影响是最小的;
                                        //3.正常我们的处理方式都是现在lion上面创建好相应的键值对，程序运行过程中不是收到create事件。
                                        if (event.getType() == EventType.NodeCreated && data.length == 0) {
                                            logger.warn(">>>>>>> lion config create, key: {}, value is empty", key);
                                        } else {
                                            timestampCache.put(path, timestamp);
                                            String value = new String(data, Constants.CHARSET);
                                            logger.info(">>>>>>> lion config changed, key: {}, value: {}", key, escape(key, value));
                                            configCache.put(key, value);
                                            if (changeList != null) {
                                                for (ConfigChange change : changeList) {
                                                    change.onChange(key, value);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    watch(path);
                                }
                            } else {
                                watch(path);
                            }
                        } else if (event.getType() == EventType.NodeDeleted) {
                            configCache.remove(key);
                            timestampCache.remove(event.getPath());
                            watch(event.getPath());
                            // if swimlane node is deleted, watch both the default node
                            String swimlane = getGroup(event.getPath());
                            if (swimlane != null) {
                                watch(getConfigPath(key));
                            }
                        }
                    } catch (Exception e) {
                        logger.error("", e);
                    } finally {
                        keyLocks.remove(key);
                    }
                }
            }

            private String getKey(String path) {
                if (path == null || !path.startsWith(Constants.CONFIG_PATH)) {
                    return null;
                }
                String key = path.substring(Constants.CONFIG_PATH.length() + 1);
                int idx = key.indexOf(Constants.PATH_SEPARATOR);
                if (idx != -1) {
                    key = key.substring(0, idx);
                }
                return key;
            }

            private String getGroup(String path) {
                String group = CuratorClient.this.getGroup();
                if (group == null) {
                    return null;
                }
                return path.endsWith(Constants.PATH_SEPARATOR + group) ? group : null;
            }

        }

        private class ConfigDataSynchronization implements Runnable {

            private long lastSyncTime;

            private int syncInterval;

            ConfigDataSynchronization() {
                syncInterval = DEFAULT_SYNC_INTERVAL;
            }

            @Override
            public void run() {
                int k = 0;
                while (!isStop) {
                    try {
                        long now = System.currentTimeMillis();
                        if (isConnected && (now - lastSyncTime > syncInterval)) {
                            syncConfig();
                            lastSyncTime = now;
                        } else {
                            Thread.sleep(1000);
                        }
                        k = 0;
                    } catch (Exception e) {
                        k++;
                        if (k > 3) {
                            try {
                                Thread.sleep(5000);
                                k = 0;
                            } catch (InterruptedException ie) {
                                break;
                            }
                        }
                        logger.error("", e);
                    }
                }
            }
        }

    }

}
