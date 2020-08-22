package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import com.dianping.lion.annotation.ConfigKey;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.resolver.*;
import com.dianping.lion.spring.ConfigMetaData;

import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.google.common.collect.Sets;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class BeanMockHandle {

    private static ThreadPool lionThreadPool = new DefaultThreadPool("lion-config-Pool",
            100, 100, new LinkedBlockingQueue<Runnable>(10000),

            new ThreadPoolExecutor.CallerRunsPolicy());
    private static ConcurrentHashMap<String, Set<ConfigMetaData>> configDataCache = new ConcurrentHashMap<>();
    static ConfigCache configCache = ConfigCache.getInstance();


    private static List<ValueResolver> valueResolvers;

    static {
        List<ValueResolver> defaultValueResolvers = new ArrayList<>(14);
        defaultValueResolvers.add(new StringResolver());
        defaultValueResolvers.add(new IntegerResolver());
        defaultValueResolvers.add(new LongResolver());
        defaultValueResolvers.add(new BooleanResolver());
        defaultValueResolvers.add(new DoubleResolver());
        defaultValueResolvers.add(new FloatResolver());
        defaultValueResolvers.add(new ShortResolver());
        defaultValueResolvers.add(new ByteResolver());
        defaultValueResolvers.add(new CharResolver());
        defaultValueResolvers.add(new DateResolver());

        if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", Thread.currentThread().getContextClassLoader())) {
            defaultValueResolvers.add(new ArrayResolver());
            defaultValueResolvers.add(new CollectionResolver());
            defaultValueResolvers.add(new MapResolver());
            defaultValueResolvers.add(new ObjectResolver());
        }

        valueResolvers = new ArrayList<>();

        valueResolvers.addAll(defaultValueResolvers);

        Iterator<ValueResolver> iterator = ServiceLoader.load(ValueResolver.class).iterator();

        List<Class<?>> defaultValueResolverClass = new ArrayList<>(defaultValueResolvers.size());
        for (ValueResolver resolver : defaultValueResolvers) {
            defaultValueResolverClass.add(resolver.getClass());
        }

        while (iterator.hasNext()) {
            ValueResolver next = iterator.next();
            int indexOf = defaultValueResolverClass.indexOf(next.getClass());
            if (indexOf == -1) {
                valueResolvers.add(0, next);

            }
        }
    }

    static public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(LionZkMockHandle.mapCache.isEmpty()){
            LionZkMockHandle.getProperty("");
        }
        if (!LionZkMockHandle.mapCache.isEmpty()) {
            lionThreadPool.execute(() -> findConfigData(bean, beanName));
            return null;
        }
        findConfigData(bean, beanName);
        return bean;
    }


    private static ConcurrentHashMap<String, Set<ConfigMetaData>> getConfigDataCache() {
        return configDataCache;
    }


    private static boolean isProxy(Object bean) {
        return AopUtils.isAopProxy(bean) || ClassUtils.isCglibProxyClass(bean.getClass());
    }

    private static Class getTargetClass(Object bean) {
        Class<?> result = null;
        if (bean instanceof TargetClassAware) {
            result = ((TargetClassAware) bean).getTargetClass();
        }
        if (result == null) {
            result = (ClassUtils.isCglibProxy(bean) ? bean.getClass().getSuperclass() : bean.getClass());
        }
        return result;
    }

    private static void findConfigData(final Object bean, final String beanName) {
        final ConcurrentHashMap<String, Set<ConfigMetaData>> result = getConfigDataCache();
        final ConcurrentHashMap<String, Set<ConfigMetaData>> current = new ConcurrentHashMap<>();

        //如果是aop代理
        Class proxyClass = bean.getClass();
        if (isProxy(bean)) {
            proxyClass = getTargetClass(bean);
        }
        final Class targetClass = proxyClass;
        final String targetClassName = targetClass.getName();

        //field
        ReflectionUtils.doWithFields(targetClass, new ReflectionUtils.FieldCallback() {
                    @Override
                    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                        if (field.getModifiers() == Modifier.FINAL) {

                            return;
                        }
                        ConfigKey configKey = field.getAnnotation(ConfigKey.class);
                        String key = configKey.value();
                        if (StringUtils.isEmpty(key)) {

                        } else {
                            boolean needChange = configKey.needChange();
                            if (!needChange) {

                            }

                            ConfigMetaData configMetaData = new ConfigMetaData(bean, beanName, field, true, needChange);

                            final Set<ConfigMetaData> allConfigMetaDataSet = result.get(key);
                            if (!CollectionUtils.isEmpty(allConfigMetaDataSet)) {
                                allConfigMetaDataSet.add(configMetaData);
                                result.put(key, allConfigMetaDataSet);
                            } else {
                                result.put(key, Sets.newHashSet(configMetaData));
                            }

                            final Set<ConfigMetaData> currentMetaDataSet = current.get(key);
                            if (!CollectionUtils.isEmpty(currentMetaDataSet)) {
                                currentMetaDataSet.add(configMetaData);
                                current.put(key, currentMetaDataSet);
                            } else {
                                current.put(key, Sets.newHashSet(configMetaData));
                            }
                        }
                    }
                },
                field -> field.isAnnotationPresent(ConfigKey.class));

        //method
        ReflectionUtils.doWithMethods(targetClass, method -> {
            if (method.getParameterTypes().length != 1) {

                return;
            }

            ConfigKey configKey = method.getAnnotation(ConfigKey.class);
            String key = configKey.value();
            if (StringUtils.isEmpty(key)) {

            } else {
                boolean needChange = configKey.needChange();
                if (!needChange) {

                }

                ConfigMetaData configMetaData = new ConfigMetaData(bean, beanName, method, false, needChange);

                final Set<ConfigMetaData> allConfigMetaDataSet = result.get(key);
                if (!CollectionUtils.isEmpty(allConfigMetaDataSet)) {
                    allConfigMetaDataSet.add(configMetaData);
                    result.put(key, allConfigMetaDataSet);
                } else {
                    result.put(key, Sets.newHashSet(configMetaData));
                }

                final Set<ConfigMetaData> currentConfigMetaDataSet = current.get(key);
                if (!CollectionUtils.isEmpty(currentConfigMetaDataSet)) {
                    currentConfigMetaDataSet.add(configMetaData);
                    current.put(key, currentConfigMetaDataSet);
                } else {
                    current.put(key, Sets.newHashSet(configMetaData));
                }
            }
        }, method -> method.isAnnotationPresent(ConfigKey.class));

        if (!CollectionUtils.isEmpty(current)) {
            //init
            Set<Map.Entry<String, Set<ConfigMetaData>>> entries = current.entrySet();
            for (Map.Entry<String, Set<ConfigMetaData>> entry : entries) {
                String key = entry.getKey();
                Set<ConfigMetaData> value = entry.getValue();
                for (ConfigMetaData configMetaData : value) {
                    Object object = configMetaData.getBean();
                    Object member = configMetaData.getMember();
                    boolean isField = configMetaData.isField();
                    if (isField) {
                        setFieldVal(object, (Field) member, key);
                    } else {
                        invokeMethod(object, (Method) member, key);
                    }

                }
            }
        }
    }


    private static void invokeMethod(Object target, Method method, String key) {
        String value = configCache.getProperty(key);
        invokeMethod(target, method, key, value, false, true);
    }

    private static void invokeMethod(Object target, Method method, String key, String value, boolean log, boolean shouldThrow) {
        String targetClassName = target.getClass().getName();
        if (isProxy(target)) {
            targetClassName = getTargetClass(target).getName();
        }
        if (value == null) {

            if (method.getAnnotation(ConfigKey.class).required()) {
                throw new RuntimeException(String.format("[Method] key:%s, value is required, but value is null in %s.%s", key, targetClassName, method.getName()));
            }
        } else {
            ValueResolver targetValueResolver = null;
            for (ValueResolver valueResolver : valueResolvers) {
                if (valueResolver.support(method)) {
                    targetValueResolver = valueResolver;
                    break;
                }
            }
            if (targetValueResolver == null) {
                throw new UnsupportedOperationException(String.format("[Method] %s is not support,key:%s,%s.%s", method.getParameterTypes()[0], key, targetClassName, method.getName()));
            }

            try {
                final Object val = targetValueResolver.resolve(value, method);
                method.setAccessible(true);
                method.invoke(target, val);

                if (log) {

                }
            } catch (Exception e) {
                if (shouldThrow) {
                    throw new RuntimeException(String.format("[Method] invoke method fail,key:%s,value:%s,%s.%s", key, value, targetClassName, method.getName()), e);
                }

            }
        }
    }


    private static void setFieldVal(Object target, Field field, String key) {
        String value = configCache.getProperty(key);
        setFieldVal(target, field, key, value, false, true);
    }

    private static void setFieldVal(Object target, Field field, String key, String value, boolean log, boolean shouldThrow) {
        String targetClassName = target.getClass().getName();
        if (isProxy(target)) {
            targetClassName = getTargetClass(target).getName();
        }
        if (value == null) {

            ReflectionUtils.makeAccessible(field);
            final Object defaultVal = ReflectionUtils.getField(field, target);
            if (defaultVal == null) {
                ConfigKey configKey = field.getAnnotation(ConfigKey.class);
                if (configKey.required()) {
                    throw new RuntimeException(String.format("[Field] key:%s, value is required, but value is null,%s.%s", key, targetClassName, field.getName()));
                }
            }
        } else {
            ValueResolver targetValueResolver = null;
            for (ValueResolver valueResolver : valueResolvers) {
                if (valueResolver.support(field)) {
                    targetValueResolver = valueResolver;
                    break;
                }
            }
            if (targetValueResolver == null) {
                throw new UnsupportedOperationException(String.format("[Field] %s is not support,key:%s,%s.%s", field.getType(), key, targetClassName, field.getName()));
            }

            try {
                Object val = targetValueResolver.resolve(value, field);
                field.setAccessible(true);
                field.set(target, val);
                if (log) {

                }
            } catch (Exception e) {
                if (shouldThrow) {
                    throw new RuntimeException(String.format("[Field] set field fail,key:%s,value:%s,%s.%s", key, value, targetClassName, field.getName()), e);
                }

            }
        }
    }
}
