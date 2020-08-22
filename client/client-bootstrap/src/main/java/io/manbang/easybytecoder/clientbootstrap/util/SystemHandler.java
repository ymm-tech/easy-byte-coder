package io.manbang.easybytecoder.clientbootstrap.util;

import io.manbang.easybytecoder.traffichandler.systemhandler.SystemClassTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * @author xujie
 */
public class SystemHandler implements ISystemClassTransformer {
    private SystemClassTransformer systemClassTransformer;
    private Instrumentation instrumentation;
    private static Logger logger = LoggerFactory.getLogger(SystemHandler.class);


    public SystemHandler(SystemClassTransformer systemClassTransformer, Instrumentation instrumentation) {
        this.systemClassTransformer = systemClassTransformer;
        this.instrumentation = instrumentation;
    }

    @Override
    public void addSystemTransformer() {
        if (systemClassTransformer == null) {
            return;
        }
        try {
            for (Class<?> listClass : systemClassTransformer.getListClass()) {
                instrumentation.retransformClasses(listClass);
                instrumentation.addTransformer(systemClassTransformer, true);
            }
        } catch (Exception e) {
            logger.error("SystemHandler addSystemTransformer err:", e);
        }
    }

    @Override
    public String getSystemClassName() {
        return systemClassTransformer.getClass().getSimpleName();
    }
}
