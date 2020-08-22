package io.manbang.easyByteCoder.plugin.simplearoundattach;


import io.manbang.easyByteCoder.plugin.simplearoundattach.mock.AroundModifier;
import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import lombok.Data;

/**
 * @author xujie
 */
@Data
public class SimpleTransformer extends BaseTransformer {

    private String relatedJarFilePath;

    @Override
    public boolean init() {
        AroundModifier aroundModifier = new AroundModifier();
        classModifiers.put(aroundModifier.getToModifyClassName(), aroundModifier);
        return true;
    }
}
