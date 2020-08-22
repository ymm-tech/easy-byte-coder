package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock;

import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import io.manbang.easyByteCoder.traffichandler.modifier.MethodModifyCode;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author GaoYang
 * 2018/12/28
 */
public class MqRocketPreventModifier extends ClassModifier {
    private static final Logger logger = LoggerFactory.getLogger(MqRocketPreventModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;

    public MqRocketPreventModifier() {
        System.out.println("[MqRocketPreventModifier]Initializing.");

        methodModifiers = new HashMap<>();
        MethodModifyCode a = new MqRocketStartPreventMockModifier();
        methodModifiers.put(a.getToModifyMethodName(), a);


    }

    @Override
    public String getToModifyClassName() {
        return "org/apache/rocketmq/client/impl/consumer/DefaultMQPushConsumerImpl";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList("io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.utils.FileUtil",
                "org.apache.commons.lang.StringUtils",
                "java.util.Properties",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.mock.runtime.ConfigRunEnableHandle");
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }

    private class MqRocketStartPreventMockModifier extends MethodModifyCode {

        @Override
        public String getToModifyMethodName() {
            return "start";
        }

        @Override
        public CtClass[] getToModifyMethodParamDecl(ClassPool pool) {
            return new CtClass[0];
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Collections.emptyList();
        }
//
//        @Override
//        public List<String> getCodeInsertAfter() {
//            return Collections.emptyList();
//        }

        @Override
        public List<String> getCodeInsertBefore() {
            return Arrays.asList(" if (ConfigRunEnableHandle.notRunAssert(\"rocketMqEnable\")) {\n" +
                    "                EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info(\"[MqRocketStartPreventMockModifier]Preventing RocketMQ consumer client start.\");\n" +
                    "                return;\n" +
                    "            }");
        }
    }
}
