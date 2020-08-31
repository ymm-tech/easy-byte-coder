package io.manbang.easybytecoder.clientbootstrap;

import com.sun.net.httpserver.HttpServer;
import io.manbang.easybytecoder.clientbootstrap.server.HttpHandlerTools;
import io.manbang.easybytecoder.clientbootstrap.server.model.AgentInfo;
import io.manbang.easybytecoder.clientbootstrap.util.SystemClassTransformerProxy;
import io.manbang.easybytecoder.clientbootstrap.util.SystemHandler;
import io.manbang.easybytecoder.traffichandler.AttachTrafficHandler;
import io.manbang.easybytecoder.traffichandler.BaseTransformer;
import io.manbang.easybytecoder.traffichandler.TrafficHandler;
import io.manbang.easybytecoder.traffichandler.annotation.process.AnnotationProcess;
import io.manbang.easybytecoder.traffichandler.systemhandler.SystemClassTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;

import static io.manbang.easybytecoder.clientbootstrap.server.HttpThreadPool.httpServerPool;

/**
 * @author GaoYang 2018/12/23
 */
public class EasyByteCoderClientBootStrap {

    private static Logger logger = LoggerFactory.getLogger(EasyByteCoderClientBootStrap.class);

    public static final String ARGS_PLUGINS = "plugins";


    public void init(Instrumentation instrumentation, String pluginBasePath, Map<String, String> agentArgs) {

        // 1. 获取生效plugin名称 与 运行参数
        List<String> pluginJarFiles = getPluginJarName(agentArgs);

        // 2. 加载相关pluging Jar文件
        ClassLoader pluginClassLoader = PluginClassLoaderFactory.createPluginClassLoader(EasyByteCoderClientBootStrap.class.getClassLoader(), new File(pluginBasePath), pluginJarFiles);

        if (pluginClassLoader == null) {
            logger.error("no easyByteCoder plugin available, please check");
            throw new IllegalStateException("no easyByteCoder plugin available, please check");
        }
        // 加载插件
        loadAndInitPlugins(instrumentation, pluginClassLoader, agentArgs);

    }

    private static List<String> getPluginJarName(Map<String, String> agentArgsMap) {
        String pluginJarsString = agentArgsMap.get(ARGS_PLUGINS.toLowerCase());
        String[] pluginNames = pluginJarsString.split(",");
        return Arrays.asList(pluginNames);
    }


    private void loadAndInitPlugins(Instrumentation instrumentation, ClassLoader pluginClassLoader, Map<String, String> agentArgsMap) {
        // 3. 获取SPI 实现类
        ServiceLoader<TrafficHandler> serviceLoader = ServiceLoader.load(TrafficHandler.class, pluginClassLoader);

        // 4. 初始化TrafficHandler.class 相关plugin的实现类， 并添加到instrument的Transformer中
        for (TrafficHandler trafficHandler : serviceLoader) {
            try {
                initPlugin(instrumentation, trafficHandler, agentArgsMap);
            } catch (Throwable throwable) {
                logger.error("init trafficHandler error, className: " + trafficHandler.getClass().getName(), throwable);
            }
        }
    }

    /**
     * 初始化插件
     *
     * @param instrumentation
     * @param agentArgsMap
     * @param trafficHandler
     */
    private void initPlugin(Instrumentation instrumentation, TrafficHandler trafficHandler, Map<String, String> agentArgsMap) {

        String jarFilePath = trafficHandler.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        logger.info("initing plugin, name: {}, jar path: {}", trafficHandler.getClass().getName(), jarFilePath);

        //trafficHandler.init(jarFilePath, agentArgsMap);

        AnnotationProcess annotationProcess = new AnnotationProcess();

        try {
            annotationProcess.process(trafficHandler, jarFilePath);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //普通类的修改
        if (annotationProcess.getBaseTransformer() instanceof BaseTransformer) {
            ClassFileTransformer transformer = annotationProcess.getBaseTransformer();
            if (transformer != null) {
                logger.info("adding transformer {}", transformer.getClass().getName());
                instrumentation.addTransformer(transformer, true);
            }
        }
        //系统类的修改
//        if (trafficHandler.getTransformer() instanceof SystemClassTransformer) {
//            addSystemTransformer(instrumentation, trafficHandler, agentArgsMap);
//        }
    }


    /**
     * attach 核心方法
     *
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        String pathName = agentArgs.replace("bootstrap.jar", "");
        List<String> pluginJarFiles = getAttachFileList(pathName);
        logger.info("agent attach start");
        ClassLoader pluginClassLoader = PluginClassLoaderFactory.createPluginClassLoader(EasyByteCoderClientBootStrap.class.getClassLoader(), new File(pathName), pluginJarFiles);
        ServiceLoader<AttachTrafficHandler> serviceLoader = ServiceLoader.load(AttachTrafficHandler.class, pluginClassLoader);
        List<AgentInfo> agentInfoList = new ArrayList<>();
        AnnotationProcess annotationProcess = new AnnotationProcess();
        for (AttachTrafficHandler trafficHandler : serviceLoader) {
            try {
                String jarFilePath = trafficHandler.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
                logger.info("initing plugin, name: {}, jar path: {}", trafficHandler.getClass().getName(), jarFilePath);
                annotationProcess.process(trafficHandler, jarFilePath);
                if (annotationProcess.getBaseTransformer() instanceof BaseTransformer) {
                    ClassFileTransformer transformer = annotationProcess.getBaseTransformer();
                    AgentInfo agentInfo = new AgentInfo();
                    agentInfo.setClassFileTransformer(transformer);
                    if (transformer != null) {
                        logger.info("adding transformer {}", transformer.getClass().getName());
                        inst.addTransformer(transformer, true);
                        for (Class<?> clazz : inst.getAllLoadedClasses()) {
                            if (annotationProcess.getClassNameList().contains(clazz.getName())) {
                                inst.retransformClasses(clazz);
                                agentInfo.getClazzs().add(clazz);
                            }
                        }
                    }
                    agentInfoList.add(agentInfo);
                }
            } catch (Throwable throwable) {
                logger.error("init trafficHandler error, className: " + trafficHandler.getClass().getName(), throwable);
            }
        }
        httpServer(inst, agentInfoList);
    }


    /**
     * 本地http服务
     */
    public static void httpServer(Instrumentation inst, List<AgentInfo> agentInfos) {
        HttpServer httpServer = null;
        try {
            logger.info("enablement doomHttpServer!!!!  port:8089");
            httpServer = HttpServer.create(new InetSocketAddress(8089), 0);
            //创建一个HttpContext，将路径为/myserver请求映射到MyHttpHandler处理器
            httpServer.createContext("/server", new HttpHandlerTools(inst, agentInfos) {});

            //设置服务器的线程池对象
            httpServer.setExecutor(httpServerPool);

            //启动服务器
            httpServer.start();

        } catch (IOException e) {
            logger.error("httpServer start error:", e);
            if (httpServer == null) {
                return;
            }
            httpServerPool.shutdown();
            httpServer.stop(1);
        }


    }

    private static List<String> getAttachFileList(String jarPath) {
        List<String> attachJarNames = new ArrayList<>();
        File file = new File(jarPath);
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {
                String fileName = fileList[i].getName();
                attachJarNames.add(fileName);
            }
        }
        return attachJarNames;
    }

    private static void addSystemTransformer(Instrumentation instrumentation, ClassFileTransformer classFileTransformer, Map<String, String> agentArgsMap) {
        try {
            SystemHandler systemHandler = new SystemHandler((SystemClassTransformer) classFileTransformer, instrumentation);
            SystemClassTransformerProxy systemClassTransformerProxy = new SystemClassTransformerProxy(systemHandler);
            systemClassTransformerProxy.addSystemTransformer();
        } catch (Throwable e) {
            logger.error("ERROR: addSystemTransformer error!", e);
        }
    }
}
