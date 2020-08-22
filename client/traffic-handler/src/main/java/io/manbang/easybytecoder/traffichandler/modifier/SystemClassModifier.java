package io.manbang.easybytecoder.traffichandler.modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.IOException;
import java.util.List;

public abstract class SystemClassModifier {

    public abstract String getToModifyMethodName();

    public abstract List<Class<?>> getRetransformClassList();

    public abstract List<String> getResourceToImport();

    public byte[] doTransform(String className, ClassPool cp, CtClass ctClass) throws IOException, CannotCompileException {
        return new byte[0];
    }
}
