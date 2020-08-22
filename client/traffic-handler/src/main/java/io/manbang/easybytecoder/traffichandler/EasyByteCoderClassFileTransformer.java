package io.manbang.easybytecoder.traffichandler;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author GaoYang 2018/12/13
 */
public interface EasyByteCoderClassFileTransformer extends ClassFileTransformer {
    /**
     * 获取plugin Jar包文件路径
     *
     * @return
     */
    String getRelatedJarFilePath();

    /**
     * 设置plugin Jar包文件路径
     *
     * @return
     */
    void setRelatedJarFilePath(String jarFilePath);
}
