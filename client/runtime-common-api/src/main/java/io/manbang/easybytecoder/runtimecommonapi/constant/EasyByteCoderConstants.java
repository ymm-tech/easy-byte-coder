package io.manbang.easybytecoder.runtimecommonapi.constant;

import java.util.Arrays;
import java.util.List;


/**
 * @author xujie
 */
public class EasyByteCoderConstants {

    public static final List<String> YMM_ACCEPT = Arrays.asList("com.ymm.", "com.ymm56.", "com.zf.", "com.mb.");

    /**
     * easyByteCoder 日志类，将easyByteCoder内的日志与应用日志隔离
     */
    public static final String EasyByteCoder_LOGGER_CLASS = "io.manbang.easybytecoder.traffichandler.logger.EasyByteCoderClientLogger";


    /**
     * easyByteCoder 中携带的fastjson类，版本较高，不依赖所注入应用是否引用fastjson，以及应用中使用的fastjosn版本
     */
    public static final String EasyByteCoder_SERIALIZER_JSON_CLASS = "io.manbang.easybytecoder.traffichandler.serializer.EasyByteCoderJSONSerializer";


    public static final String EasyByteCoder_SERIALIZER_GSON_CLASS = "io.manbang.easybytecoder.traffichandler.serializer.EasyByteCoderGSONSerializer";


}
