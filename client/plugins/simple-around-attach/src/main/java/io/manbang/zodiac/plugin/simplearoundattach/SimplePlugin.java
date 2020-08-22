package io.manbang.easyByteCoder.plugin.simplearoundattach;

import com.google.auto.service.AutoService;
import io.manbang.easyByteCoder.traffichandler.AttachTrafficHandler;
import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import io.manbang.easyByteCoder.traffichandler.annotation.ModifyClassName;
import io.manbang.easyByteCoder.traffichandler.annotation.ResourceToImport;

import java.util.List;
import java.util.Map;


/**
 * @author xujie
 */
@AutoService(AttachTrafficHandler.class)
@ModifyClassName("com/ymm/trade/dataacquisition/server/service/test/TestService")
@ResourceToImport()
public class SimplePlugin implements AttachTrafficHandler {

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

    @Override
    public List<String> getClassNameList() {
        return transformer.getClassNameList();
    }
}
