package io.manbang.easybytecoder.traffichandler.systemhandler;

import io.manbang.easybytecoder.traffichandler.modifier.SystemClassModifier;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * 系统相关类的修改
 *
 * @Author: sundaoming
 * @CreateDate: 2019/4/22 15:23
 */
public abstract class SystemClassTransformer implements ClassFileTransformer {

    private static final Logger logger = LoggerFactory.getLogger(SystemClassTransformer.class);


    public Map<String, SystemClassModifier> methodModifiers = new HashMap<>();

    public abstract boolean init();


    private ClassLoader doomClassLoader;


    public Set<Class<?>> getListClass() {
        Set<Class<?>> set = new HashSet<>();
        for (Map.Entry<String, SystemClassModifier> stringSystemClassModifierEntry : methodModifiers.entrySet()) {
            set.addAll(stringSystemClassModifierEntry.getValue().getRetransformClassList());
        }
        return set;
    }

    @Override
    public byte[] transform(final ClassLoader originLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classFileBuffer) {

        CtClass.debugDump = "./dump";
        if (className == null) {
            return classFileBuffer;
        }
        ClassPool cp = new ClassPool(true);
        cp.appendClassPath(new LoaderClassPath(originLoader));

        CtClass currentCtClass = null;
        try {
            currentCtClass = cp.makeClass(new ByteArrayInputStream(classFileBuffer));
        } catch (Throwable e) {
            logger.error("SystemClassTransformer makeClass error", e);
            return classFileBuffer;
        }


        byte[] result = null;
        SystemClassModifier systemClassModifier = methodModifiers.get(className);
        if (systemClassModifier != null) {
            for (String packageName : systemClassModifier.getResourceToImport()) {
                cp.importPackage(packageName);
            }
            try {
                result = methodModifiers.get(className).doTransform(className, cp, currentCtClass);
            } catch (Throwable e) {
                logger.error("SystemClassTransformer doTransformForDateClass error", e);
                return classFileBuffer;
            }
            return result;
        }

        return classFileBuffer;
    }

}
