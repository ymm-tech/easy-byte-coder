package io.manbang.easyByteCoder.plugin.simplecount;

import com.google.auto.service.AutoService;
import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import io.manbang.easyByteCoder.traffichandler.TrafficHandler;

import java.util.Map;


/**
 * @author xujie
 */
@AutoService(TrafficHandler.class)
public class SimplePlugin implements TrafficHandler {

    private BaseTransformer transformer;

    @Override
    public boolean init(String jarFile, Map<String, String> args) {
        this.transformer = new SimpleTransformer();
        transformer.init();
        transformer.setRelatedJarFilePath(jarFile);
        return true;
    }


    @Override
    public BaseTransformer getTransformer() {
        return transformer;
    }
}
