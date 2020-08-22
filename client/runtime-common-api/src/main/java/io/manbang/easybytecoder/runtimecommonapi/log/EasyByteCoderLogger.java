package io.manbang.easybytecoder.runtimecommonapi.log;

/**
 * @author GaoYang
 * 2018/12/23
 */



public interface EasyByteCoderLogger {
    void debug(String var1);

    void debug(String format, Object arg);

    void debug(String format, Object arg1, Object arg2);

    void debug(String format, Object... arguments);

    void info(String var1);

    void warn(String var1);

    void error(String var1);

    void error(String var1, Throwable e);

    void error(String format, Object... arguments);

    void trace(String var1);
}
