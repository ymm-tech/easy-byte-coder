package io.manbang.easybytecoder.client;


import io.manbang.easybytecoder.client.utils.ArgsParser;
import io.manbang.easybytecoder.client.utils.BootstrapClassLoaderFactory;
import io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool;
import io.manbang.easybytecoder.runtimecommonapi.utils.StringUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author GaoYang 2018/12/23
 */
public class Agent {

    /**
     * 制定plugin路径参数名
     */
    public static final String ARHS_PLUGIN_PATH = "pluginPath";

    /**
     * plugin Jar包路径
     */
    public static String BASE_PLUGIN_PATH;

    /**
     * client bootstrap实例
     */
    public static volatile Object bootstrapObj;

    /**
     * java agent 的入口函数 会先于应用的main方法执行 主要进行了easyByteCoder空间的初始化： log初始化，各中间件plugin的初始化
     * 以及设置instrument的trnsformer； 每一个类在加载进JVM 前，都会经由各plugin的transformer进行匹配修改
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        // 参数解析
        Map<String, String> agentArgsMap = ArgsParser.parse(agentArgs);
        // 环境准备
        prepareEnv(agentArgsMap, instrumentation);
        // thread context classloader 特殊处理，使easyByteCoder使用的log4j 与应用本身完全隔离互不干扰
        URLClassLoader easyByteCoderLoader = BootstrapClassLoaderFactory.createBootClassLoader(BASE_PLUGIN_PATH, null);
        ClassLoader originLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 初始化easyByteCoder bootstrap 与plugin
            Thread.currentThread().setContextClassLoader(easyByteCoderLoader);
            // 初始化 应用与easyByteCoder的桥接类 EasyByteCoderResourceObjectPool，使用该类可以连通easyByteCoder空间与应用空间，相互访问
            EasyByteCoderResourceObjectPool.init(easyByteCoderLoader);
            loadAndRegisterPlugins(easyByteCoderLoader, agentArgsMap, instrumentation);
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("ERROR: bootStrap init failed!", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originLoader);
        }

    }



    private static void prepareEnv(Map<String, String> agentArgsMap, Instrumentation instrumentation) {
        // 1. 将当前jar包中的其他类提升到bootstrap Classloader 中进行加载
        String agentJarFilePath = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File agentFile = new File(agentJarFilePath);
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(agentFile));
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("add to bootstrap class path error:", e);
        }

        // 2. 获取plugin包存放路径
        BASE_PLUGIN_PATH = getPluginPath(agentArgsMap, agentFile);
    }

    private static String getPluginPath(Map<String, String> agentArgsMap, File agentFile) {
        // 默认从args中获取
        String path = agentArgsMap.get(ARHS_PLUGIN_PATH.toLowerCase());
        if (StringUtils.isNotBlank(path)) {
            return path;
        }
        // 未传入，默认认为plugin包与agent包在同一个目录下
        return agentFile.getParent();
    }

    /**
     * 加载plugin插件
     *
     * @param easyByteCoderClassLoader
     * @param agentArgsMap
     * @param instrumentation
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private static void loadAndRegisterPlugins(URLClassLoader easyByteCoderClassLoader, Map<String, String> agentArgsMap, Instrumentation instrumentation)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info("start loading  plugins");

        // 实例化EasyByteCoderClientBootStrap并调用init方法
        Class<?> bootstrapClass = easyByteCoderClassLoader.loadClass("io.manbang.easybytecoder.clientbootstrap.EasyByteCoderClientBootStrap");

        bootstrapObj = bootstrapClass.newInstance();

        Method initMethod = bootstrapClass.getMethod("init", Instrumentation.class, String.class, Map.class);
        // 调用init方法
        initMethod.invoke(bootstrapObj, instrumentation, BASE_PLUGIN_PATH, agentArgsMap);

        EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info("loading easyByteCoder plugins successfully");
    }


}
