package io.manbang.easybytecoder.traffichandler.modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.util.List;

public abstract class ClassModifier {

    /**
     * 获取要修改的类名
     *
     * @return
     */
    public abstract String getToModifyClassName();

    /**
     * 获取修改该类时，需要额外引入的包名
     *
     * @param originLoader
     * @return
     */
    public abstract List<String> getResourceToImport(ClassLoader originLoader);


    /**
     * 获取某方法的修改实现
     *
     * @param methodDesc
     * @return
     */
    public abstract MethodModifyCode getMethodModifier(String methodDesc);

    /**
     * 获取修改该类时，需要加入的全局字段信息
     *
     * @param clazz
     * @param pool
     * @return
     * @throws Exception
     */
    public List<CtField> getFieldsToAdd(CtClass clazz, ClassPool pool) throws Exception {
        return null;
    }

    /**
     * 获取 待修改类 实现的接口类class
     *
     * @return
     */
    public String getImplInterfaceClass() {
        return "java.lang.Object";
    }

//    /**
//     * 用于校验某个中间件代码应该拦截却没有被拦截，这里一般用能唯一识别中间件的包名
//     *
//     * @return
//     */
//    public String getPluginPackageNameForCheck(){
//        return "";
//    }


}
