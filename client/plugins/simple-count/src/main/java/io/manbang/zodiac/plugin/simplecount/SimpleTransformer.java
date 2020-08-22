package io.manbang.easyByteCoder.plugin.simplecount;


import io.manbang.easyByteCoder.plugin.simplecount.mock.BeanCreateTimeModifier;
import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import lombok.Data;

/**
 * java类简单作用描述
 * @author xujie
 */
@Data
public class SimpleTransformer extends BaseTransformer {

    private String relatedJarFilePath;

    @Override
    public boolean init() {
        BeanCreateTimeModifier beanCreateTimeModifier = new BeanCreateTimeModifier();
        classModifiers.put(beanCreateTimeModifier.getToModifyClassName(), beanCreateTimeModifier);
        return true;
    }
}
