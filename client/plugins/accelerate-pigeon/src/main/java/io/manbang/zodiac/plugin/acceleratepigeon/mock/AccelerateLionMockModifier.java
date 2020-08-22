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

public class AccelerateLionMockModifier extends ClassModifier {

    private static final Logger logger = LoggerFactory.getLogger(AccelerateLionMockModifier.class);
    private Map<String, MethodModifyCode> methodModifiers;


    public AccelerateLionMockModifier() {
        methodModifiers = new HashMap<>(2);
        MethodModifyCode beanMethodModifier = new AccelerateLionMockModifier.BeanMethodModifier();
        methodModifiers.put(beanMethodModifier.getToModifyMethodName(), beanMethodModifier);
        MethodModifyCode beanMethodModifier2 = new AccelerateLionMockModifier.BeanMethodModifier2();
        methodModifiers.put(beanMethodModifier2.getToModifyMethodName(), beanMethodModifier2);

    }

    @Override
    public String getToModifyClassName() {
        return "com/dianping/lion/client/ConfigCache";
    }

    @Override
    public List<String> getResourceToImport(ClassLoader originLoader) {
        return Arrays.asList(
                "io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.mock.runtime.LionZkMockHandle",
                "org.apache.commons.lang.StringUtils",
                "com.alibaba.fastjson.JSON",
                "com.alibaba.fastjson.JSONObject",
                "com.alibaba.fastjson.serializer.SerializerFeature",
                "io.manbang.easybytecoder.plugin.acceleratepigeon.utils.FileUtil"
        );
    }

    @Override
    public MethodModifyCode getMethodModifier(String methodDesc) {
        return methodModifiers.get(methodDesc);
    }


    private class BeanMethodModifier extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "getProperty";
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
                      "  String res= LionZkMockHandle.getProperty($1);\n" +
                            "\n" +
                            "            if(FileUtil.fileIsExists(FileUtil.customDirectory(),\"lion.json\")  ){\n" +
                            "                return res;\n" +
                            "            }"
            );
        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Arrays.asList(
                    "LionZkMockHandle.CuratorClientSetForCache($1,$_);"
            );
        }


    }

    private class BeanMethodModifier2 extends MethodModifyCode {
        @Override
        public String getToModifyMethodName() {
            return "init";
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




            return Arrays.asList("  if(  FileUtil.fileIsExists(FileUtil.customDirectory(),\"lion.json\")){\n" +
                    "                EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error(\" lion.json  existing  skip  lion init \");\n" +
                    "                return;\n" +
                    "            }");

        }

        @Override
        public List<String> getCodeInsertAfter() {
            return Arrays.asList("LionZkMockHandle.SetLocalZkCache();");
        }
    }

}
