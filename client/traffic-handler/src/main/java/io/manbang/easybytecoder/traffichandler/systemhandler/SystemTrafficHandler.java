package io.manbang.easybytecoder.traffichandler.systemhandler;

import java.util.Map;

public interface SystemTrafficHandler {
    boolean init(String jarFile, Map<String, String> args);

    SystemClassTransformer getTransformer();
}
