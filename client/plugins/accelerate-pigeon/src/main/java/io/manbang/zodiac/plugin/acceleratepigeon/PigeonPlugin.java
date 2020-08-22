package io.manbang.easyByteCoder.plugin.acceleratepigeon;

import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import io.manbang.easyByteCoder.traffichandler.TrafficHandler;

import java.lang.instrument.ClassFileTransformer;
import java.util.Map;


/**
 * @author xujie
 */
public class PigeonPlugin implements TrafficHandler {

    private String runMode="mock";
    private String jarFilePath;
    private BaseTransformer transformer;

    @Override
    public boolean init(String jarFile, Map<String, String> args) {
        this.jarFilePath = jarFile;

        String defineArgs = args.get("runmode");

        System.out.println("PigenoPlugin runMode:" + runMode);


        if (!runMode.equals(defineArgs)) {
            return false;
        }
        transformer = new PigeonMockTransformer();

        transformer.init();
        transformer.setRelatedJarFilePath(jarFilePath);

        return true;
    }

    @Override
    public ClassFileTransformer getTransformer() {
        return transformer;
    }
}
