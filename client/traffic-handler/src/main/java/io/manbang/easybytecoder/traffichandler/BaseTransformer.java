package io.manbang.easybytecoder.traffichandler;

import io.manbang.easybytecoder.traffichandler.modifier.ClassModifier;
import io.manbang.easybytecoder.traffichandler.utils.StringUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * @author GaoYang 2018/12/10
 */
public abstract class BaseTransformer implements EasyByteCoderClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(BaseTransformer.class);


    /**
     * @key: classname 待修改的类名
     * @value: modifier 类修改器
     */
    protected Map<String, ClassModifier> classModifiers = new HashMap<>();

    /**
     * @key: interfaceClass 待修改的接口类名，所有该接口的实现类都会被修改
     * @value: modifier 类修改器
     */
    protected Map<String, ClassModifier> interfaceImplClassModifiers = new HashMap<>();

    public BaseTransformer() {
    }


    /**
     * 每个plugin 的初始化方法
     *
     * @return
     */
    public abstract boolean init();

    /**
     * 所有需要加载的类
     * @return
     */
    public List<String> getClassNameList() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, ClassModifier> stringClassModifierEntry : classModifiers.entrySet()) {
            if(stringClassModifierEntry==null){
                continue;
            }
            String className = stringClassModifierEntry.getKey().replace("/", ".");
            list.add(className);
        }
        return list;
    }

    /**
     * 获取plugin jar包文件路径
     *
     * @return String
     */
    @Override
    public abstract String getRelatedJarFilePath();

    /**
     * 设置plugin jar包文件路径
     *
     * @param jarFilePath
     */
    @Override
    public abstract void setRelatedJarFilePath(String jarFilePath);

    /**
     * instrument 技术核心方法： 所有被jvm加载的类，都会调用已注册的transformer，进行类方法的修改拦截
     */
    @Override
    public byte[] transform(ClassLoader originLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            // 1. 过滤无需处理的类
            if (className == null) {
                return classfileBuffer;
            }
            // 2. 检查是否定义此类的修改代码
            ClassModifier modifier = findMatchedClassModifier(className);
            ClassPool cp = new ClassPool(true);
            cp.appendClassPath(new LoaderClassPath(originLoader));
            CtClass currentCtClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));

            if (modifier == null) {
                String interfaceClassName = findInterfaceImplClassModifier(className, currentCtClass, cp);
                modifier = interfaceImplClassModifiers.get(interfaceClassName);
            }
            if (modifier == null) {
                return classfileBuffer;
            }

            // 3. 注入plugin jar包
            importRelatedJar(originLoader);
            // 4. 添加import 包声明
            List<String> packagesToImport = modifier.getResourceToImport(originLoader);

            for (String res : packagesToImport) {
                cp.importPackage(res);
            }
            cp.importPackage("io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool");
            cp.importPackage("io.manbang.easybytecoder.runtimecommonapi.utils.PluginContext");
            // 5. 执行类修改
            return DoTransformer.INSTANCE.execute(className, modifier, cp, currentCtClass);

        } catch (Exception e) {
            logger.error("transform className:{}   error:", className, e);
        }
        return classfileBuffer;
    }


    /**
     * 加载相关的jar文件
     *
     * @param originLoader
     */
    private void importRelatedJar(ClassLoader originLoader) {
        if (StringUtils.isNotEmpty(getRelatedJarFilePath())) {
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                method.setAccessible(true);
                File file = new File(getRelatedJarFilePath());
                method.invoke(originLoader, new Object[]{file.toURI().normalize().toURL()});
            } catch (Exception e) {
                logger.error("loading relatedJarFile:{} error:", getRelatedJarFilePath(), e);
            }
        }
    }

    private ClassModifier findMatchedClassModifier(String className) {

        // fast path
        ClassModifier matchedClassModifier = classModifiers.get(className);

        if (matchedClassModifier != null) {
            return matchedClassModifier;
        }

        // slow path. may lose some performance.
        // fix the shaded package problem. ie: com.google.protobuf.BlockingRpcChannel -> org.apache.hadoop.hbase.shaded.com.google.protobuf.BlockingRpcChannel
        Iterator<Map.Entry<String, ClassModifier>> it = classModifiers.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, ClassModifier> entry = it.next();
            // 规避entry.getKey()为空的情况
            String targetClassName = entry.getKey();
            if (targetClassName == null || targetClassName.isEmpty()) {
                continue;
            }
            if (className.endsWith(targetClassName)) {
                return entry.getValue();
            }
        }

        return null;

    }

    private String findInterfaceImplClassModifier(String className, CtClass ctClass, ClassPool classPool) {

        for (String interfaceClass : interfaceImplClassModifiers.keySet()) {
            if (doCheckImplementCertainClass(interfaceClass, classPool, ctClass)) {
                logger.info("Found implement of interface:" + interfaceClass + " ToModifyClassName:" + className);
                return interfaceClass;
            }
        }

        return null;
    }


    /**
     * 检查当前类 是否是指定interface类的实现
     *
     * @param interfaceClass
     * @param cp
     * @param ctClass
     * @return
     */
    private boolean doCheckImplementCertainClass(String interfaceClass, ClassPool cp, CtClass ctClass) {
        try {
            if (!ctClass.isInterface()) {
                CtClass interfaceCtClass = cp.get(interfaceClass);
                return ctClass.subtypeOf(interfaceCtClass);
            }
        } catch (NotFoundException e) {
            logger.error("doCheckImplementCertainClass Failed! NotFound:" + interfaceClass);
        }
        return false;
    }


}
