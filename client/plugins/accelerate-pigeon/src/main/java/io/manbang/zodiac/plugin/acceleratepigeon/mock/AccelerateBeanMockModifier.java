package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AccelerateBeanMockModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AccelerateBeanMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AccelerateBeanMockModifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifyCode beanMethodModifier = new BeanMethodModifier();
        methodModifiers.put(beanMethodModifier.getToModifyMethodName(), beanMethodModifier);
    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/lion/spring/SpringLionConfig";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.plugin.acceleratepigeno.mock.runtime.BeanMockHandle");
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }

    @Override
    public List<CtField> getFieldsToAdd(CtClass clazz, ClassPool pool) throws Exception {
        return super.getFieldsToAdd(clazz, pool);
    }

    @Override
    public String getImplInterfaceClass() {
        return super.getImplInterfaceClass();
    }


    private class BeanMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "postProcessBeforeInitialization";
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
            return Arrays.asList(
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"accelerate bean receive  mock message\");" +
                            " if (!this.beanName.equals(beanName)) {" +
                            "BeanMockHandle.postProcessBeforeInitialization($1,$2); " +
                            "}"
                            + "return  $1;"
            );
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();
        }


    }
}
