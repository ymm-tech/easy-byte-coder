package io.manbang.easybytecoder.client.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author GaoYang 2018/12/23
 */
public class ArgsParser {

    public static Map<String, String> parse(String args) {
        if (isEmpty(args)) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new HashMap<String, String>();

        Scanner scanner = new Scanner(args);
        scanner.useDelimiter(":");

        while (scanner.hasNext()) {
            String token = scanner.next();
            int assign = token.indexOf('=');

            if (assign == -1) {
                map.put(token.toLowerCase(), "");
            } else {
                String key = token.substring(0, assign);
                String value = token.substring(assign + 1);
                map.put(key.toLowerCase(), value);
            }
        }
        scanner.close();
        return Collections.unmodifiableMap(map);
    }

    private static boolean isEmpty(String args) {
        return args == null || args.isEmpty();
  }


}
