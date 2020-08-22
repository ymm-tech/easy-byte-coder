package io.manbang.easybytecoder.traffichandler.modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.util.List;
import java.util.Map;

public abstract class MethodModifyCode {
    /**
     * easyByteCoder Context处理代码段
     *
     * @return
     */
    @Deprecated
    public String handleContext() {
        return "";
    }

    /**
     * 获取需要修改的方法名MethodName
     *
     * @return
     */
    public abstract String getToModifyMethodName();

    /**
     * 是否需要进行参数匹配
     *
     * @return
     */
    public boolean ifNeedCheckParam() {
        return true;
    }

    /**
     * 该方法Modifier是否需要个性化匹配校验
     *
     * @return
     */
    public boolean ifNeedCheckParamByOwn() {
        return false;
    }

    /**
     * 各方法Modifier个性化参数匹配校验
     *
     * @param argTypes
     * @param classPool
     * @return
     */
    public boolean checkParamByOwn(CtClass[] argTypes, ClassPool classPool) {
        return true;
    }

    /**
     * 获取需要修改的方法 入参类型表
     *
     * @return
     */
    public abstract CtClass[] getToModifyMethodParamDecl(ClassPool pool);

    /**
     * 在方法最前面，嵌入的代码段
     *
     * @return
     */
    public abstract List<String> getCodeInsertBefore();

    /**
     * 在方法最后，嵌入的代码段
     *
     * @return
     */
    public abstract List<String> getCodeInsertAfter();


    /**
     * 将方法使用tryCatch包围
     * @return
     */
    public CatchCode getCatchCode() {
        return new CatchCode("{EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error(\"[easyByteCoder Maybe Affected]invoke method Failed!, error:\",$e); throw $e; }");
    }

    /**
     * 在方法体替换，替换整个method body
     *
     * @return
     */
    public String getCodeToSetBody(CtMethod method) {
        return "";
    }

    /**
     * 在方法体替换，替换整个constructor body
     *
     * @return
     */

    public String getCodeToSetBody(CtConstructor constructor) {
        return "";
    }

    public Map<String, CtClass> localVariables() {
        return null;
    }
}
