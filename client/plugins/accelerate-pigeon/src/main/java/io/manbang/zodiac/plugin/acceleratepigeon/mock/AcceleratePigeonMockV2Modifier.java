package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AcceleratePigeonMockV2Modifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AcceleratePigeonMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AcceleratePigeonMockV2Modifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifyCode pigenoMethodModifier = new AcceleratePigeonMockV2Modifier.PigenoMethodModifier();
        methodModifiers.put(pigenoMethodModifier.getToModifyMethodName(), pigenoMethodModifier);

    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/pigeon/remoting/ServiceFactory";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList("io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
        "com.dianping.pigeon.remoting.common.codec.SerializerFactory",
                "com.dianping.pigeon.remoting.invoker.InvokerBootStrap"
        );
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }

    private class PigenoMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "getService";
        }



        @Override
        public CtClass[] getToModifyMethodParamDecl(ClassPool pool) {
            CtClass[] paramList = new CtClass[1];
            try {
                paramList[0] = pool.get("com.dianping.pigeon.remoting.invoker.config.InvokerConfig");
            } catch (Exception e) {
                logger.error("[CatRecordModifier]Exception while getting to modified method sign", e);
            }
            return paramList;
        }

        @Override
        public List<String> getCodeInsertBefore() {
            return Arrays.asList(
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"acceleratepigeno receive  mock message\"); " +
                            "InvokerBootStrap.startup();\n" +
                            "return SerializerFactory.getSerializer($1.getSerialize()).proxyRequest($1);"
                           );
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();
        }

    }


}
