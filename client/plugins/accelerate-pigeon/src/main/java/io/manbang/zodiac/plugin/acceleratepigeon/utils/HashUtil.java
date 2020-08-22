package io.manbang.easyByteCoder.plugin.acceleratepigeon.utils;

/**
 * @author GaoYang
 * 2019/1/12
 */
public class HashUtil {

        public static long toHash(String s){
            long seed = 131;
            long hash=0;
            for (int i = 0; i< s.length(); i++){
                hash = (hash * seed) + s.charAt(i);
            }
            return hash;
        }

}
