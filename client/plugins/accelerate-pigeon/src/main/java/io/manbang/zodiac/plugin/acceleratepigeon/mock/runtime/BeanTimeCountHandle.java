package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.FileUtil;
import io.manbang.easyByteCoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BeanTimeCountHandle {

    public static volatile ConcurrentHashMap<String, Long> timeBeanMap = new ConcurrentHashMap<>();
    private static volatile boolean executorServiceStatus=false;

    public static void putTimeBeanMap(String key, long value) {
        timeBeanMap.put(key, value);
    }

    public static void timeMapToFile() {
        List<Map.Entry<String, Long>> list = new ArrayList<>();
        list.addAll(timeBeanMap.entrySet());
        List<Map.Entry<String, Long>> restList = getSortedHashtableByValue(list);
        String beanCreateTime = EasyByteCoderResourceObjectPool.getJsonSerializer().serialize(restList);

        FileUtil.fWriter(FileUtil.customDirectory(), "creatBeanTime", beanCreateTime);
    }


    public static List<Map.Entry<String, Long>> getSortedHashtableByValue(List<Map.Entry<String, Long>> list) {
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                //按照value值，重小到大排序
                //return o1.getValue() - o2.getValue();
                //按照value值，从大到小排序
                return Math.toIntExact(o2.getValue() - o1.getValue());
                //按照value值，用compareTo()方法默认是从小到大排序
                //return o1.getValue().compareTo(o2.getValue());
            }
        });
        return list;
    }


    public static void launch() {
        if(executorServiceStatus){
            return;
        }

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                timeMapToFile();
            }
        }, 10, 20, TimeUnit.SECONDS);

        executorServiceStatus=true;
    }
}
