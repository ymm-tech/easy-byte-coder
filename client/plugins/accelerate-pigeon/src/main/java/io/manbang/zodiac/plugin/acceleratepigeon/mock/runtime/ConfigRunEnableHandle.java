package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import io.manbang.easyByteCoder.plugin.acceleratepigeon.utils.FileUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * @author xujie
 */
public class ConfigRunEnableHandle {
    public static boolean notRunAssert(String confName) {
        Properties doomConfigProperties = FileUtil.getDoomConfigProperties();
        if (StringUtils.isNotEmpty(doomConfigProperties.getProperty(confName))) {
            boolean rpcMockEnable = Boolean.valueOf(doomConfigProperties.getProperty(confName));
            if (!rpcMockEnable) {
                return true;
            }
        }
        return false;
    }

    public static boolean runAssert(String confName) {
        return !notRunAssert(confName);
    }

}
