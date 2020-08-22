package io.manbang.easybytecoder.runtimecommonapi.utils;



import io.manbang.easybytecoder.runtimecommonapi.constant.EasyByteCoderConstants;
import io.manbang.easybytecoder.runtimecommonapi.log.EasyByteCoderLogger;
import io.manbang.easybytecoder.runtimecommonapi.serialize.EasyByteCoderSerializer;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author GaoYang 2018/12/23
 */
public class EasyByteCoderResourceObjectPool {

    private static Map<String, Object> objectCache = new HashMap<>();
    private static Map<String, Method> methodCache = new HashMap<>();
    private static volatile URLClassLoader bootClassLoader;

    public static void init(URLClassLoader classLoader) {
        EasyByteCoderResourceObjectPool.bootClassLoader = classLoader;
        getFromCache(EasyByteCoderConstants.EasyByteCoder_LOGGER_CLASS);
        getFromCache(EasyByteCoderConstants.EasyByteCoder_SERIALIZER_JSON_CLASS);
    }

    public static EasyByteCoderLogger getEasyByteCoderLogger() {
        return (EasyByteCoderLogger) getFromCache(EasyByteCoderConstants.EasyByteCoder_LOGGER_CLASS);
    }

    public static EasyByteCoderSerializer getJsonSerializer() {
        return (EasyByteCoderSerializer) getFromCache(EasyByteCoderConstants.EasyByteCoder_SERIALIZER_JSON_CLASS);
    }

    public static EasyByteCoderSerializer getGsonSerializer() {
        return (EasyByteCoderSerializer) getFromCache(EasyByteCoderConstants.EasyByteCoder_SERIALIZER_GSON_CLASS);
    }


    /**
     * 获取easyByteCoder空间加载的类
     *
     * @param classname 需要的easyByteCoder空间内的类名
     * @return easyByteCoder空间内的类
     */
    public static Class getEasyByteCoderClass(String classname) {
        try {
            return bootClassLoader.loadClass(classname);
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger()
                    .error("ERROR loadClass is null:getEasyByteCoderClass:" + classname, e);
            return null;
        }
    }

    /**
     * 获取“应用空间”运行时加载的类
     *
     * @param classname 需要的 “应用空间”内的类名
     * @return 应用空间内的类
     */
    public static Class getRuntimeClass(String classname) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(classname);
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger()
                    .error("ERROR loadClass is null:getRuntimeClass:" + classname, e);
            return null;
        }
    }


    /**
     * 获取runtime中运行的类方法，并缓存起来
     */
    public static Method getNotOverloadRuntimeMethodFromCache(
            String className, String methodName, Class<?>... parameterTypes) {
        Method method = methodCache.get(className + ":" + methodName);
        try {
            if (method == null) {
                Class baseTransformClass = getRuntimeClass(className);
                method = baseTransformClass.getMethod(methodName, parameterTypes);
                methodCache.put(className + ":" + methodName, method);
            }
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("getNotOverloadRuntimeMethodFromCache error:", e);
        }
        return method;
    }


    /**
     * 获取easyByteCoder中的类，并缓存起来
     */
    public static Object getFromCache(String packageClassName) {
        Object obj = objectCache.get(packageClassName);
        if (obj != null) {
            return obj;
        }

        try {
            if (EasyByteCoderResourceObjectPool.bootClassLoader == null) {
                EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("ERROR is null:Agent.bootstrapObj");
                return null;
            }
            Class logFactoryClass = bootClassLoader.loadClass(packageClassName);
            if (logFactoryClass == null) {
                EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("logFactoryClass is null");
            } else {
                Object objNew = logFactoryClass.newInstance();
                if (objNew == null) {
                    EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("logFactoryClass is null");
                } else {
                    objectCache.put(packageClassName, objNew);
                }
            }
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("getFromCache error:", e);
        }
        return objectCache.get(packageClassName);
    }


}
