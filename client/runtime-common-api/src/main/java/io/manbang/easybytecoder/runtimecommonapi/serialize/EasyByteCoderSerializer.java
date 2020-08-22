/**
 * ymm56.com Inc.
 * Copyright (c) 2013-2019 All Rights Reserved.
 */
package io.manbang.easybytecoder.runtimecommonapi.serialize;

import java.util.List;

/**
 *
 * @author zhouhujin
 * @version $Id: DoomSerializer.java, v 0.1 2019-02-21 14:05 zhouhujin Exp $$
 */
public interface EasyByteCoderSerializer {

    <T> T deserialize(String json, Class<T> clazz);

    Object deserialize(String json);

    String serialize(Object obj);

    String serializeWithClassInfo(Object object);

    Object deserializeToArray(String json);

    <T> List<T> deserializeToArray(String json, Class<T> clazz);
}
