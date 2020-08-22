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

/**
 * @author xujie
 */
public class SpringLionConfigOnApplicationEvent extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AccelerateZkMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public SpringLionConfigOnApplicationEvent() {
        methodModifiers = new HashMap<>(1);
        MethodModifier methodModifier = new MethodModifier();
        methodModifiers.put(methodModifier.getToModifyMethodName(), methodModifier);
    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/lion/spring/SpringLionConfig";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.utils.FileUtil"
        );
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }


    private class MethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "onApplicationEvent";
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
            return Arrays.asList("EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error(\"  ServiceInitializeListener SpringLionConfigOnApplicationEvent \");" +
                    "  FileUtil.updateProperties(\"lionCover\",\"true\");\n" +
                    "            FileUtil.updateProperties(\"lionRecordGetting\",\"10\");"
            );
        }


        @Override
        public List<String> getCodeInsertBefore() {
            return Arrays.asList("");
        }
    }
}
