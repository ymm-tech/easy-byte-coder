package io.manbang.easybytecoder.traffichandler.modifier;

import javassist.CtClass;

/**
 * 待增加的catch代码
 *
 * @Author: sundaoming
 * @CreateDate: 2019/5/14 11:14
 */
public class CatchCode {

    String codes;

    CtClass catchType;

    public CatchCode(String codes) {
        this.codes = codes;
    }

    public CatchCode(String codes, CtClass catchType) {
        this.codes = codes;
        this.catchType = catchType;
    }

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }

    public CtClass getCatchType() {
        return catchType;
    }

    public void setCatchType(CtClass catchType) {
        this.catchType = catchType;
    }
}
