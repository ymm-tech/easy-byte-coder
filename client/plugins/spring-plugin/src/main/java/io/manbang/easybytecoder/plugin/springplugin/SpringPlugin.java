package io.manbang.easybytecoder.plugin.springplugin;

import com.google.auto.service.AutoService;
import io.manbang.easybytecoder.traffichandler.TrafficHandler;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyClassName;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyMethod;
import io.manbang.easybytecoder.traffichandler.annotation.ResourceToImport;


/**
 * @author xujie
 */
@AutoService(TrafficHandler.class)
@ModifyClassName("org/springframework/context/support/AbstractApplicationContext")
@ResourceToImport({"io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
        "io.manbang.easybytecoder.plugin.springplugin.modify.runtime.RegisterAspectAdvice"})
public class SpringPlugin implements TrafficHandler {

    @ModifyMethod(methodName = "invokeBeanFactoryPostProcessors")
    public String modify() {
        return "RegisterAspectAdvice.registerAspect($1);";
    }
}
