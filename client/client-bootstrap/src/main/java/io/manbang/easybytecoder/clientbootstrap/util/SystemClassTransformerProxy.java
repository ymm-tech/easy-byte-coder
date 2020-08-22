package io.manbang.easybytecoder.clientbootstrap.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujie
 */
public class SystemClassTransformerProxy {

    private ISystemClassTransformer systemClassTransformer;
    private static Logger logger = LoggerFactory.getLogger(SystemClassTransformerProxy.class);

    public SystemClassTransformerProxy(ISystemClassTransformer systemClassTransformer) {
        this.systemClassTransformer = systemClassTransformer;
    }


    public void addSystemTransformer() {
        logger.warn("className： "+systemClassTransformer.getSystemClassName()+" Modifying the system method");
        systemClassTransformer.addSystemTransformer();
    }

}
