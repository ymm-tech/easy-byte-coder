package io.manbang.easybytecoder.traffichandler.logger;

import io.manbang.easybytecoder.runtimecommonapi.log.EasyByteCoderLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author GaoYang 2018/12/23
 */
public class EasyByteCoderClientLogger implements EasyByteCoderLogger {
    private static final Logger logger = LoggerFactory.getLogger(EasyByteCoderClientLogger.class);

    @Override
    public void debug(String var1) {
        logger.debug(var1);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void info(String var1) {
        logger.info(var1);
    }

    @Override
    public void warn(String var1) {
        logger.warn(var1);
    }

    @Override
    public void error(String var1) {
        logger.error(var1);
    }

    @Override
    public void error(String var1, Throwable e) {
        logger.error(var1, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void trace(String var1) {
        logger.trace(var1);
    }

}
