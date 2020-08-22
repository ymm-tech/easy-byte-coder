package io.manbang.easyByteCoder.plugin.systemtime.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.SystemClassModifier;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author xujie
 */
public class SystemTimeModifier extends SystemClassModifier {
    private static final Logger logger = LoggerFactory.getLogger(SystemTimeModifier.class);

    @Override
    public String getToModifyMethodName() {
        return "java/util/Date";
    }

    @Override
    public List<Class<?>> getRetransformClassList() {
        List<Class<?>> retransformClasses = new ArrayList<>(3);
        retransformClasses.add(Date.class);
        return retransformClasses;
    }


    @Override
    public List<String> getResourceToImport() {
        return Arrays.asList(
                "io.manbang.easybytecoder.runtimecommonapi.utils.SystemModifierUtil"
        );
    }


    @Override
    public byte[] doTransform(String className, ClassPool cp, CtClass ctClass) throws IOException, CannotCompileException {
        try {
            for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
                if (constructor.getParameterTypes().length == 0) {
                    ctClass.removeConstructor(constructor);
                    CtConstructor defaultConstructor = CtNewConstructor.make("public " + ctClass.getSimpleName() + "() { $0.fastTime = SystemModifierUtil.currentTimeMillis();}", ctClass);
                    ctClass.addConstructor(defaultConstructor);
                }
            }
            logger.trace("add extended defaultConstruct for class" + className);
        } catch (Throwable e) {
            logger.error("add defaultConstruct for class " + className, e);
        }

        return ctClass.toBytecode();
    }
}
