/**
 * ymm56.com Inc.
 * Copyright (c) 2013-2019 All Rights Reserved.
 */
package io.manbang.easybytecoder.traffichandler.serializer;

import com.google.gson.Gson;
import io.manbang.easybytecoder.runtimecommonapi.serialize.EasyByteCoderSerializer;

import java.util.List;

/**
 *
 * @author zhouhujin
 * @version $Id: easyByteCoderGSONSerializer.java, v 0.1 2019-02-22 17:04 zhouhujin Exp $$
 */
public class EasyByteCoderGSONSerializer implements EasyByteCoderSerializer {

    private static final Gson GSON = new Gson();

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    @Override
    public Object deserialize(String json) {
        return GSON.fromJson(json, Object.class);
    }

    @Override
    public String serialize(Object obj) {
        return GSON.toJson(obj);
    }

    @Override
    @Deprecated
    public String serializeWithClassInfo(Object object) {
        return GSON.toJson(object);
    }

    @Override
    @Deprecated
    public Object deserializeToArray(String json) {
        return GSON.fromJson(json, Object.class);
    }

    @Override
    @Deprecated
    public <T> List<T> deserializeToArray(String json, Class<T> clazz) {
        // TODO
        return null;
    }
}
