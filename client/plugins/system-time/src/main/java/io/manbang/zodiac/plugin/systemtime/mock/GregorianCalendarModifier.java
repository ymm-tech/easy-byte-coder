package io.manbang.easyByteCoder.plugin.systemtime.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.SystemClassModifier;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author xujie
 */
public class GregorianCalendarModifier extends SystemClassModifier {
    private static final Logger logger = LoggerFactory.getLogger(SystemTimeModifier.class);
    @Override
    public String getToModifyMethodName() {
        return "java/util/GregorianCalendar";
    }

    @Override
    public List<Class<?>> getRetransformClassList() {
        List<Class<?>> retransformClasses = new ArrayList<>(3);
        retransformClasses.add(GregorianCalendar.class);
        return retransformClasses;
    }

    @Override
    public List<String> getResourceToImport() {
        return Arrays.asList( "io.manbang.easybytecoder.runtimecommonapi.utils.SystemModifierUtil");
    }

    @Override
    public byte[] doTransform(String className, ClassPool cp, CtClass ctClass) throws IOException, CannotCompileException {
        try {
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            for (CtMethod method : declaredMethods) {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getClassName().equals("java.lang.System") && m.getMethodName().equals("currentTimeMillis")) {
                            m.replace("{ $_ = SystemModifierUtil.currentTimeMillis(); }");
                        }
                    }
                });
            }

            CtConstructor[] declaredConstructors = ctClass.getDeclaredConstructors();
            for (CtConstructor method : declaredConstructors) {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getClassName().equals("java.lang.System") && m.getMethodName().equals("currentTimeMillis")) {
                            m.replace("{ $_ = SystemModifierUtil.currentTimeMillis(); }");
                        }
                    }
                });
            }

        } catch (Exception e) {
            logger.error("doTransformForSystemClass Failed!", e);
        }

        return ctClass.toBytecode();
    }
}
