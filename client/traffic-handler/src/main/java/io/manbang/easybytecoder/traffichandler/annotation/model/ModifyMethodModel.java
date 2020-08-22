package io.manbang.easybytecoder.traffichandler.annotation.model;

/**
 * @author xujie
 */

import io.manbang.easybytecoder.traffichandler.modifier.CatchCode;
import javassist.CtClass;

import java.util.List;
import java.util.Map;

/**
 * - LocalVariables 在方法中加入局部变量
 * - Before 在方法运行之前修改
 * - After 在方法返回之前修改
 * - Catch 自定义捕获方法异常
 * - Body 替换整个方法体
 *
 * @author xujie
 */
public class ModifyMethodModel {
    Map<String, CtClass> localVariablesCode;
    List<String> beforeCode;
    List<String> afterCode;
    String bodyCode;
    CatchCode catchCode;
    boolean checkParam;
    String[] MethodParamDecl;


    public boolean isCheckParam() {
        return checkParam;
    }

    public void setCheckParam(boolean checkParam) {
        this.checkParam = checkParam;
    }

    public String[] getMethodParamDecl() {
        return MethodParamDecl;
    }

    public void setMethodParamDecl(String[] methodParamDecl) {
        MethodParamDecl = methodParamDecl;
    }

    public Map<String, CtClass> getLocalVariablesCode() {
        return localVariablesCode;
    }

    public void setLocalVariablesCode(Map<String, CtClass> localVariablesCode) {
        this.localVariablesCode = localVariablesCode;
    }

    public List<String> getBeforeCode() {
        return beforeCode;
    }

    public void setBeforeCode(List<String> beforeCode) {
        this.beforeCode = beforeCode;
    }

    public List<String> getAfterCode() {
        return afterCode;
    }

    public void setAfterCode(List<String> afterCode) {
        this.afterCode = afterCode;
    }

    public String getBodyCode() {
        return bodyCode;
    }

    public void setBodyCode(String bodyCode) {
        this.bodyCode = bodyCode;
    }

    public CatchCode getCatchCode() {
        return catchCode;
    }

    public void setCatchCode(CatchCode catchCode) {
        this.catchCode = catchCode;
    }
}

