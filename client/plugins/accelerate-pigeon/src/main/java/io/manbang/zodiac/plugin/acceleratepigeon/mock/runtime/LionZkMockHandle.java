package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;


import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.FileUtil;
import io.manbang.easyByteCoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool;
import io.manbang.easyByteCoder.runtimecommonapi.utils.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @author xujie
 */
public class LionZkMockHandle {

    public static volatile ConcurrentHashMap<String, String> mapCache = new ConcurrentHashMap<>();

    private static volatile boolean executorServiceStatus = false;

    private static ExecutorService mapCacheProcessThreadPool = Executors.newSingleThreadExecutor();

    private final static String NULL = "__NULL__";


    public static String getProperty(String str) {
        if (mapCache.isEmpty()) {
            String jsonString = FileUtil.readFile(FileUtil.customDirectory(), "lion");
            if (StringUtils.isEmpty(jsonString)) {
                return StringUtils.EMPTY;
            }

            Map<String, Object> objects = (Map<String, Object>) EasyByteCoderResourceObjectPool.getJsonSerializer().deserialize(jsonString);
            for (Map.Entry<String, Object> stringObjectEntry : objects.entrySet()) {
                mapCache.put(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
            }
        }
        String res = mapCache.get(str);
        if (NULL.equals(res)) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().warn("LionZkMockHandle  getProperty return " + NULL + "  key=" + str + "value=null");
            return null;
        }
        EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info("LionZkMockHandle  getProperty return  key=" + str + "  value=:" + res);
        return res;
    }


    public static void CuratorClientSetForCache(String str, String path) {
        if (path != null) {
            mapCache.put(str, path);
        } else {
            mapCache.put(str, NULL);
        }
    }


    public static void SetLocalZkCache() {
        if (executorServiceStatus) {
            return;
        }
        Integer seconds = 0;

        if (!FileUtil.fileIsExists(FileUtil.customDirectory(), "lion.json")) {
            FileUtil.updateProperties("lionCover","false");
        }

        Properties doomConfigProperties = FileUtil.getDoomConfigProperties();
        if (doomConfigProperties.get("lionRecordGetting") != null) {
            seconds = Integer.valueOf((String) doomConfigProperties.get("lionRecordGetting"));
        }
        if (seconds == 0) {
            seconds = 60;
        }

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Properties doomConfigProperties = FileUtil.getDoomConfigProperties();
                Boolean lionCoverValue = false;
                if (doomConfigProperties != null && doomConfigProperties.size() > 0) {
                    String lionCoverStrValue = doomConfigProperties.getProperty("lionCover");
                    lionCoverValue = Boolean.parseBoolean(lionCoverStrValue);
                }

                if (!lionCoverValue) {
                    return;
                }
                if (mapCache == null || mapCache.size() == 0) {
                    return;
                }

                mapCacheProcessThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, String> stringStringEntry : mapCache.entrySet()) {
                            ConfigCache configCacheInstance = ConfigCache.getInstance();
                            String syncPath = configCacheInstance.getProperty(stringStringEntry.getKey());
                            if (syncPath != null) {
                                mapCache.put(stringStringEntry.getKey(), syncPath);
                            } else {
                                mapCache.put(stringStringEntry.getKey(), NULL);
                            }
                        }
                    }
                });
                String jsonString = EasyByteCoderResourceObjectPool.getJsonSerializer().serialize(mapCache);
                FileUtil.fWriter(FileUtil.customDirectory(), "lion", jsonString);

            }
        }, seconds, 5, TimeUnit.SECONDS);

        executorServiceStatus = true;
    }

}
