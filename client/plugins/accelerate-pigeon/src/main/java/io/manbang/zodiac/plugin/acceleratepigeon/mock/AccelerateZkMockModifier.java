package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccelerateZkMockModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AccelerateZkMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AccelerateZkMockModifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifyCode ZkMethodModifier = new ZkMethodModifier();
        MethodModifyCode ZkMethodModifier2 = new ZkMethodModifier2();
        MethodModifyCode ZkMethodModifier3 = new ZkMethodModifier3();

        methodModifiers.put(ZkMethodModifier3.getToModifyMethodName(), ZkMethodModifier3);
        methodModifiers.put(ZkMethodModifier.getToModifyMethodName(), ZkMethodModifier);
        methodModifiers.put(ZkMethodModifier2.getToModifyMethodName(), ZkMethodModifier2);

    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/pigeon/registry/zookeeper/CuratorClient";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.plugin.acceleratepigeno.mock.runtime.ZkMockHandle",
                "io.manbang.easybytecoder.traffichandler.utils.JedisUtils",
                "redis.clients.jedis.Jedis");
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


    private class ZkMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "get";
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
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"accelerate Zk receive  mock message\");" +
                            "String res=ZkMockHandle.CuratorClientGetForCache($$);" +
                            "if(res!=null && !res.isEmpty()){return res;}" +
                            "return ZkMockHandle.get($0,$$);"

            );
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Arrays.asList("ZkMockHandle.CuratorClientSetForCache($1,$_);");
        }


    }



    private class ZkMethodModifier3 extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "set";
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
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"accelerate Zk receive  mock message\");"
            );
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Arrays.asList("ZkMockHandle.CuratorClientSet($$);");
        }

    }




    private class ZkMethodModifier2 extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "newCuratorClient";
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
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"accelerate Zk receive  mock message\");");
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Arrays.asList("ZkMockHandle.SetLocalZkCache();");
        }





    }


}
