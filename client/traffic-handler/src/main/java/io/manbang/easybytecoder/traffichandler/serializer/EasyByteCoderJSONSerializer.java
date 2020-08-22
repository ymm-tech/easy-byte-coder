/**
 * ymm56.com Inc.
 * Copyright (c) 2013-2019 All Rights Reserved.
 */
package io.manbang.easybytecoder.traffichandler.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.manbang.easybytecoder.runtimecommonapi.serialize.EasyByteCoderSerializer;

import java.util.List;

/**
 *
 * @author zhouhujin
 * @version $Id: easyByteCoderJSONSerializer.java, v 0.1 2019-02-21 14:07 zhouhujin Exp $$
 */
public class EasyByteCoderJSONSerializer implements EasyByteCoderSerializer {


    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        return JSONObject.parseObject(json, clazz);
    }

    @Override
    public Object deserialize(String json) {
        return JSONObject.parse(json);
    }

    @Override
    public String serialize(Object obj) {
        return JSON.toJSONString(obj);
    }

    @Override
    public String serializeWithClassInfo(Object object) {
        return JSON.toJSONString(
                object,
            SerializerFeature.WriteMapNullValue,
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteClassName);
    }

    @Override
    public Object deserializeToArray(String json) {
        return JSON.parseArray(json);
    }

    @Override
    public <T> List<T> deserializeToArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

}
