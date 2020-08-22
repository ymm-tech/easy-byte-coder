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

public class AcceleratePigeonResponseMockModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AcceleratePigeonMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AcceleratePigeonResponseMockModifier() {
        methodModifiers = new HashMap<>(1);
        MethodModifyCode pigenoMethodModifier = new AcceleratePigeonResponseMockModifier.PigenoMethodModifier();
        methodModifiers.put(pigenoMethodModifier.getToModifyMethodName(), pigenoMethodModifier);

    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/pigeon/remoting/invoker/service/ServiceInvocationProxy";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.mock.runtime.PigeonResponseHandle"
        );
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }

    private class PigenoMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "invoke";
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
        public List<String> getCodeInsertAfter() {

            return Arrays.asList(
                    "EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().debug(\"AcceleratePigeonResponseMockModifier receive  mock message\"); " +
                            " PigeonResponseHandle.saveContent($0.invokerConfig,$2,$_);"
            );
        }

        @Override
        public List<String> getCodeInsertBefore() {

            return Arrays.asList(" if(PigeonResponseHandle.getContent($0.invokerConfig,$2)!=null){\n" +
                    "                            return PigeonResponseHandle.getContent($0.invokerConfig,$2);\n" +
                    "                        }\n");
        }

    }


}
