package io.manbang.easybytecoder.plugin.simplehotfix;

import com.google.auto.service.AutoService;


import io.manbang.easybytecoder.traffichandler.AttachTrafficHandler;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyClassName;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyMethod;
import io.manbang.easybytecoder.traffichandler.annotation.ResourceToImport;
import io.manbang.easybytecoder.traffichandler.annotation.constant.CodePatternEnum;


/**
 * @author xujie
 */

@AutoService(AttachTrafficHandler.class)
@ResourceToImport({
        "io.manbang.easybytecoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool",
        "io.manbang.easybytecoder.plugin.simplehotfix.mock.runtime.FixHandle"
})
@ModifyClassName("io/manbang/helloworld/HelloWorld")
public class SimplePlugin implements AttachTrafficHandler {
    
    @ModifyMethod(methodName = "runPrint", pattern = CodePatternEnum.Before)
    public String modifyBefore() {
        return "$1=FixHandle.fixModel($1);";
    }

}
