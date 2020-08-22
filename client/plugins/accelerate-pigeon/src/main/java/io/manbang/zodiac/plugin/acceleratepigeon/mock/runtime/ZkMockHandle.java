package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.google.common.collect.ImmutableMap;
import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.DateStyle;
import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.data.Stat;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZkMockHandle {


    public static volatile ConcurrentHashMap<String, String> mapCache = new ConcurrentHashMap<>();
    public static String day = DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD);

    public static String CuratorClientGetForCache(String str, boolean bool) {
        return CuratorClientGetForCache(str, null);
    }

    public static String CuratorClientGetForCache(String str) {
        return CuratorClientGetForCache(str, true);
    }


    public static String CuratorClientGetForCache(String str, Stat stat) {

        if (mapCache.isEmpty()) {

            File file = null;
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                file = new File("./zk"+day);
                if (!file.exists()) {
                    return null;
                }

                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);

                ConcurrentHashMap<String, String> obj = null;
                while ((obj = (ConcurrentHashMap<String, String>) ois.readObject()) != null) {
                    mapCache.putAll(obj);
                }
                return mapCache.get(str);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                if (file != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        return mapCache.get(str);
    }


    public static void CuratorClientSetForCache(String str, String path) {
        if (StringUtils.isNotEmpty(path)) {
            mapCache.put(str, path);
        }
    }


    public static void CuratorClientSet(String str, Object obj) {
        if (obj != null) {
            mapCache.put(str, obj.toString());
            return;
        }
        mapCache.put(str, null);

    }


    public static void CuratorClientSet(String str, Object obj, int vlaue) {
        if (obj != null) {
            mapCache.put(str, obj.toString());
            return;
        }
        mapCache.put(str, null);
    }


    public static void SetLocalZkCache() {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("job-Local-Zk-cache").daemon(true).build());
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                //声明一个文件（创建文件）
                File file = null;
                //声明文件输出字节流
                FileOutputStream fos = null;
                //声明对象处理流
                ObjectOutputStream oos = null;
                try {
                    file = new File("./zk"+day);
                    fos = new FileOutputStream(file);
                    oos = new ObjectOutputStream(fos);
                    //向文件中写入对象的数据
                    oos.writeObject(mapCache);
                    oos.writeObject(null);
                    //清空缓冲区
                    oos.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //关闭资源
                        fos.close();
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 100, 20, TimeUnit.SECONDS);
    }

    public static String get(CuratorClient curatorClient, String path) throws Exception {
        return get(curatorClient, path, true);
    }

    public static String get(CuratorClient curatorClient, String path, Stat stat) throws Exception {
        if (curatorClient.exists(path, false)) {
            byte[] bytes = curatorClient.getClient().getData().storingStatIn(stat).forPath(path);
            String value = new String(bytes, "UTF-8");
            mapCache.put(path, value);
            return value;
        } else {
            return null;
        }
    }

    public static String get(CuratorClient curatorClient, String path, boolean watch) throws Exception {
        if (curatorClient.exists(path, watch)) {
            byte[] bytes = curatorClient.getClient().getData().forPath(path);
            String value = new String(bytes, "UTF-8");
            mapCache.put(path, value);
            return value;
        } else {
            return null;
        }
    }


    public static void setSupportNewProtocol(CuratorRegistry curatorRegistry, String serviceAddress, String serviceName, boolean support)
            throws RegistryException {
        try {
            String protocolPath = Utils.getProtocolPath(serviceAddress);
            Stat stat = new Stat();
            String info = curatorRegistry.getCuratorClient().get(protocolPath, stat);

            if (info != null) {
                Map<String, Boolean> infoMap = Utils.getProtocolInfoMap(info);
                infoMap.put(serviceName, support);
                curatorRegistry.getCuratorClient().set(protocolPath, Utils.getProtocolInfo(infoMap), stat.getVersion());
            } else {
                Map<String, Boolean> infoMap = ImmutableMap.of(serviceName, support);
                curatorRegistry.getCuratorClient().create(protocolPath, Utils.getProtocolInfo(infoMap));
            }

        } catch (Throwable e) {

        }
    }
}
