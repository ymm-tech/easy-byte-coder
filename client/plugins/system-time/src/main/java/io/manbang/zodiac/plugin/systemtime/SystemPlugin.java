package io.manbang.easyByteCoder.plugin.systemtime;

import com.google.auto.service.AutoService;
import io.manbang.easyByteCoder.traffichandler.systemhandler.SystemClassTransformer;
import io.manbang.easyByteCoder.traffichandler.TrafficHandler;

import java.util.Map;


/**
 * @author xujie
 */
@AutoService(TrafficHandler.class)
public class SystemPlugin implements TrafficHandler {

    private SystemClassTransformer transformer;

    @Override
    public boolean init(String jarFile, Map<String, String> args) {
        this.transformer = new SystemTransformer();
        transformer.init();
        return true;
    }


    @Override
    public SystemClassTransformer getTransformer() {
        return transformer;
    }
}
