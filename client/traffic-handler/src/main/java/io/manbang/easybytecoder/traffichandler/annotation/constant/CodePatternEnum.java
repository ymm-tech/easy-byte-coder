package io.manbang.easybytecoder.traffichandler.annotation.constant;

/**
 * @author xujie
 */

/**
 * - LocalVariables 在方法中加入局部变量
 * - Before 在方法运行之前修改
 * - After 在方法返回之前修改
 * - Catch 自定义捕获方法异常
 * - Body 替换整个方法体
 */
public enum CodePatternEnum {
    LocalVariables,
    Before,
    After,
    Catch,
    Body
}
