package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.*;

/**
 * java类简单作用描述
 *
 * @Author: sundaoming
 * @CreateDate: 2019/3/10 14:05
 */
public class BinlogClientMockModifierSecond extends ClassModifier {

    private Map<String, MethodModifyCode> methodModifiers;

    public BinlogClientMockModifierSecond(){
        methodModifiers = new HashMap<>();
        MethodModifyCode initMethodModifier = new BinlogInitMethodModifier();
        methodModifiers.put(initMethodModifier.getToModifyMethodName(), initMethodModifier);
    }

    @Override
    public String getToModifyClassName() {
        return "com/ymm/binlog/center/agent/job/Job";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList("io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool");
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }

    private class BinlogInitMethodModifier extends MethodModifyCode {

        @Override
        public String getToModifyMethodName() {
            return "init";
        }

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
            return new ArrayList<>(0);
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return new ArrayList<>(0);
        }

        @Override
        public String getCodeToSetBody(CtMethod method) {
            return "{ EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info(\"[BinlogInitMethodModifier]Preventing Binlog Job init.\");"
                    + "return; }";
        }
    }
}
