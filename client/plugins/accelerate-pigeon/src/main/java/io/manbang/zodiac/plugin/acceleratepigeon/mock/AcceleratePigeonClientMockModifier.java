package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AcceleratePigeonClientMockModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AcceleratePigeonClientMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AcceleratePigeonClientMockModifier() {
        methodModifiers = new HashMap<>(1);
     //   MethodModifyCode pigenoMethodModifier = new PigenoMethodModifier();
        MethodModifyCode pigenoMethodModifier2 = new PigenoMethodModifier2();
        //methodModifiers.put(pigenoMethodModifier.getToModifyMethodName(), pigenoMethodModifier);
        methodModifiers.put(pigenoMethodModifier2.getToModifyMethodName(), pigenoMethodModifier2);

    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/pigeon/remoting/invoker/ClientManager";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.plugin.acceleratepigeno.mock.runtime.PigeonMockHandle");
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


    private class PigenoMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "connect";
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
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"acceleratepigeno receive  mock message\");"
                            + "return;");
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();
        }
    }


    private class PigenoMethodModifier2 extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "registerClients";
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
            return Collections.emptyList();
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();
        }


        @Override
        public String getCodeToSetBody(CtMethod method) {
            return "{return PigeonMockHandle.registerClients($0,$1,$2,$3);}";
        }
    }


}
