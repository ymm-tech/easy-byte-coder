package io.manbang.easybytecoder.clientbootstrap.server.model;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xujie
 */
public class AgentInfo {
    ClassFileTransformer classFileTransformer;
    List<Class<?>> clazzs;
    public AgentInfo() {
        this.clazzs = new ArrayList<>();
    }

    public ClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
    }

    public void setClassFileTransformer(ClassFileTransformer classFileTransformer) {
        this.classFileTransformer = classFileTransformer;
    }

    public List<Class<?>> getClazzs() {
        return clazzs;

    }

    public void setClazzs(List<Class<?>> clazzs) {
        this.clazzs = clazzs;
    }


}
