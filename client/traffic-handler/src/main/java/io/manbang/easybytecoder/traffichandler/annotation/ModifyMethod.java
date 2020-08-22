package io.manbang.easybytecoder.traffichandler.annotation;


import io.manbang.easybytecoder.traffichandler.annotation.constant.CodePatternEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xujie
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyMethod {
    String methodName();

    String[] paramDecl() default {};

    CodePatternEnum pattern() default CodePatternEnum.Before;

    boolean checkParam() default false;


}
