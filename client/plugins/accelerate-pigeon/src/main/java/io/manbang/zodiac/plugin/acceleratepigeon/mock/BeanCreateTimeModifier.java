package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanCreateTimeModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(BeanCreateTimeModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public BeanCreateTimeModifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifier methodModifier = new MethodModifier();
        methodModifiers.put(methodModifier.getToModifyMethodName(), methodModifier);
    }

    @Override
    public String getToModifyClassName() {
        return "org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.mock.runtime.BeanTimeCountHandle"
        );
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }


    private class MethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "createBean";
        }

        /**
         * 不需要进行参数匹配
         *
         * @return
         */
        @Override
        public boolean ifNeedCheckParam() {
            return false;
        }

        @Override
        public CtClass[] getToModifyMethodParamDecl(ClassPool pool) {
            return new CtClass[0];
        }

        @Override
        public List<String> getCodeInsertBefore() {
            return Arrays.asList("startTime = System.currentTimeMillis();");
        }

        @Override
        public List<String> getCodeInsertAfter() {

            return Arrays.asList("EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error( \"createBean  \"+$1+\"  time is :\" + (System.currentTimeMillis() - startTime) + \"ms\");" +
                    "BeanTimeCountHandle.putTimeBeanMap($1,(System.currentTimeMillis() - startTime));" +
                    "BeanTimeCountHandle.launch();"
            );
        }

        @Override
        public Map<String, CtClass> localVariables() {
            HashMap<String, CtClass> map = new HashMap<>(1);
            map.put("startTime", CtClass.longType);
            return map;
        }


    }


}
