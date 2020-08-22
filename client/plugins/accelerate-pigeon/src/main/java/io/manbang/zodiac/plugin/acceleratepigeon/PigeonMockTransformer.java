package io.manbang.easyByteCoder.plugin.acceleratepigeon;



import io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.*;
import io.manbang.easyByteCoder.traffichandler.BaseTransformer;
import io.manbang.easyByteCoder.traffichandler.modifier.ClassModifier;
import lombok.Data;

/**
 * java类简单作用描述
 */
@Data
public class PigeonMockTransformer extends BaseTransformer {

    private String relatedJarFilePath;

    @Override
    public boolean init() {

        ClassModifier accelerateLionMockModifier = new AccelerateLionMockModifier();

        classModifiers.put(accelerateLionMockModifier.getToModifyClassName(), accelerateLionMockModifier);

        ClassModifier acceleratePigenoMockV2Modifier = new AcceleratePigeonMockV2Modifier();
        classModifiers.put(acceleratePigenoMockV2Modifier.getToModifyClassName(), acceleratePigenoMockV2Modifier);

        AcceleratePigeonResponseMockModifier acceleratePigeonResponseMockModifier = new AcceleratePigeonResponseMockModifier();
        classModifiers.put(acceleratePigeonResponseMockModifier.getToModifyClassName(), acceleratePigeonResponseMockModifier);

        SpringLionConfigOnApplicationEvent pigeonOnApplicationEvent = new SpringLionConfigOnApplicationEvent();
        classModifiers.put(pigeonOnApplicationEvent.getToModifyClassName(), pigeonOnApplicationEvent);

        BinlogClientMockModifier binlogClientMockModifier = new BinlogClientMockModifier();
        classModifiers.put(binlogClientMockModifier.getToModifyClassName(), binlogClientMockModifier);

        BinlogClientMockModifierSecond binlogClientMockModifierSecond = new BinlogClientMockModifierSecond();
        classModifiers.put(binlogClientMockModifierSecond.getToModifyClassName(), binlogClientMockModifierSecond);

        MqRocketPreventModifier mqRocketPreventModifier = new MqRocketPreventModifier();
        classModifiers.put(mqRocketPreventModifier.getToModifyClassName(), mqRocketPreventModifier);

        return true;
    }
}
