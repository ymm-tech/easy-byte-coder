package io.manbang.easyByteCoder.plugin.simplearoundattach.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author xujie
 */
public class AroundModifier extends ClassModifier {
    private static final Logger logger = LoggerFactory.getLogger(AroundModifier.class);

    private Map<String, MethodModifyCode> methodModifiers;

    public AroundModifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifyCode beanMethodModifier = new MethodModifier();
        methodModifiers.put(beanMethodModifier.getToModifyMethodName(), beanMethodModifier);
    }


    @Override
    public String getToModifyClassName() {
        return "com/ymm/trade/dataacquisition/server/service/test/TestService";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Collections.emptyList();
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }


    private class MethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "test";
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
            return Arrays.asList("   System.out.println(\"AroundModifier  AroundModifier  attach\");");
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();

        }
    }
}
