package io.manbang.easyByteCoder.plugin.systemtime;



import io.manbang.easyByteCoder.plugin.systemtime.mock.GregorianCalendarModifier;
import io.manbang.easyByteCoder.plugin.systemtime.mock.SystemTimeModifier;
import io.manbang.easyByteCoder.traffichandler.systemhandler.SystemClassTransformer;
import lombok.Data;

/**
 * java类简单作用描述
 * @author xujie
 */
@Data
public class SystemTransformer extends SystemClassTransformer {

    private String relatedJarFilePath;

    @Override
    public boolean init() {
        SystemTimeModifier systemTimeModifier = new SystemTimeModifier();
        methodModifiers.put(systemTimeModifier.getToModifyMethodName(), systemTimeModifier);
        GregorianCalendarModifier gregorianCalendarModifier = new GregorianCalendarModifier();
        methodModifiers.put(gregorianCalendarModifier.getToModifyMethodName(), gregorianCalendarModifier);
        return true;
    }
}
