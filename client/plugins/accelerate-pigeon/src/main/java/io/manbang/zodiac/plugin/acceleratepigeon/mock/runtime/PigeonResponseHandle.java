package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.ymm.common.rpc.YmmResult;
import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.FileUtil;
import io.manbang.easyByteCoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


import java.lang.reflect.Method;
import java.util.Properties;


/**
 * @author xujie
 */
public class PigeonResponseHandle {
    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }

    public static void saveContent(InvokerConfig<?> invokerConfig, Method method, Object returnObj) {

        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String pathParent = FileUtil.customDirectory() + "pigeonResponse/";
        String footer = ".json";
        String fileName = invokerConfig.getUrl() + "#" + methodName;


        if (noSaveAssert(fileName)) {
            return;
        }


        if (FileUtil.fileIsExists(pathParent, fileName + footer)) {
            return;
        }
        if (doNotHandleAssertion(methodName, parameterTypes)) {
            return;
        }

        if (returnObj == null) {
            return;
        }
        String jsonString = JSON.toJSONString(
                returnObj,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteClassName,
                SerializerFeature.WriteNullBooleanAsFalse);
        FileUtil.fWriter(pathParent, invokerConfig.getUrl() + "#" + methodName, jsonString);

    }

    /**
     * 判断是否需要保存mock的rpc返回数据
     *
     * @param fileName
     * @return
     */
    private static boolean noSaveAssert(String fileName) {
        Properties doomConfigProperties = FileUtil.getDoomConfigProperties();

        //mock开关rpcMockEnable
        if (StringUtils.isNotEmpty(doomConfigProperties.getProperty("rpcMockEnable"))) {
            boolean rpcMockEnable = Boolean.valueOf(doomConfigProperties.getProperty("rpcMockEnable"));
            if (!rpcMockEnable) {
                return true;
            }
        }

        String rpcExcludeAddress = doomConfigProperties.getProperty("rpcExcludeAddress");
        if (StringUtils.isNotEmpty(rpcExcludeAddress)) {
            String[] rpcExcludeAddressArray = rpcExcludeAddress.split(",");
            return ArrayUtils.contains(rpcExcludeAddressArray, fileName);

        }
        return false;
    }

    private static boolean doNotHandleAssertion(String methodName, Class<?>[] parameterTypes) {

        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return true;
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return true;
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return true;
        }
        return false;
    }


    public static Object getContent(InvokerConfig<?> invokerConfig, Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (doNotHandleAssertion(methodName, parameterTypes)) {
            return null;
        }

        boolean isAuthenticationService = "http://service.ymm.com/uc-auth-center/authenticationService_1.0.0#authentication".equals(invokerConfig.getUrl() + "#" + method.getName());
        if (isAuthenticationService && ConfigRunEnableHandle.runAssert("authenticationMockEnable")) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().info("[MOCK] authenticationMockEnable = true  In response to the results ");
            return JSON.parseObject("{\"@type\":\"com.ymm.uc.uac.common.facade.dto.result.AuthenticationResult\",\"errorMsg\":\"\",\"responseMap\":null,\"resultCode\":\"SUCCESS\",\"success\":false}", method.getReturnType());
        }
        String json = FileUtil.readFile(FileUtil.customDirectory() + "pigeonResponse/", invokerConfig.getUrl() + "#" + method.getName());
        if (StringUtils.isEmpty(json)) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("PigeonResponseHandle getContent  json is NUll");
            return null;
        }
        EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("PigeonResponseHandle getContent pigeonResponse url:" + invokerConfig.getUrl() + "#" + method.getName());

        if (method.getReturnType().isAssignableFrom(YmmResult.class)) {
            YmmResult ymmResult = (YmmResult) JSON.parseObject(json, method.getReturnType());
            YmmResult ymmResult1 = YmmResult.succResult(ymmResult.getData());
            return ymmResult1;
        }

        return JSON.parseObject(json, method.getReturnType());

    }

    private Object getReturn(Class<?> returnType) {
        if (returnType == byte.class) {
            return (byte) 0;
        } else if (returnType == short.class) {
            return (short) 0;
        } else if (returnType == int.class) {
            return 0;
        } else if (returnType == boolean.class) {
            return false;
        } else if (returnType == long.class) {
            return 0L;
        } else if (returnType == float.class) {
            return 0.0f;
        } else if (returnType == double.class) {
            return 0.0d;
        } else {
            return null;
        }
    }

}
