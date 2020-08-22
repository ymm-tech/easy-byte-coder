package io.manbang.easybytecoder.runtimecommonapi.utils;

/**
 * 系统函数修改后的实现
 *
 * @Author: sundaoming
 * @CreateDate: 2019/4/22 15:31
 */
public class SystemModifierUtil {

    private static long now = System.currentTimeMillis();

    private static long nowNano = System.nanoTime();




    public static long currentTimeMillis(){
        long freezeTime = now;
        if ( freezeTime > 0) {
            return freezeTime;
        }else{
            long realTime = now + (System.nanoTime() - nowNano)/1000000L;
            //System.out.println("WARNING! SystemModifierUtil:currentTimeMillis>> useing real currentTime : " + realTime);
            return realTime;
        }
    }



    public static long getRealCurrenttime() {
        return now + (System.nanoTime() -nowNano)/1000000L;
    }

    @Deprecated
    public static long syncToFreezeTime(long timeStamp){
        if (now > 0) {
            long userDefinedDiffTime = timeStamp - now;

            if (userDefinedDiffTime >= 0) {
                userDefinedDiffTime = (userDefinedDiffTime/1000) *1000;
            } else {
                userDefinedDiffTime = (userDefinedDiffTime/1000) *1000 -1000L;
            }

            long newTimeStamp = userDefinedDiffTime + now;
            if (timeStamp != newTimeStamp) {
                System.out.println("xxxxxxxxxxxx originTimeStamp: " + timeStamp + " newTimeStamp: "
                    + newTimeStamp);
            }
            return newTimeStamp;
        }else{
            return timeStamp;
        }
    }

    @Deprecated
    public static long syncToFreezeTimeForMockMode(long timeStamp){
        long timeCoodinate = now;
        if (timeCoodinate > 0) {
            if (Math.abs(timeStamp-timeCoodinate) > 1000) {

            }
            long userDefinedDiffTime = timeStamp - now;

            if (userDefinedDiffTime >= 0) {
                userDefinedDiffTime = (userDefinedDiffTime/1000) *1000;
            } else {
                userDefinedDiffTime = (userDefinedDiffTime/1000) *1000 -1000L;
            }

            long newTimeStamp = userDefinedDiffTime + now;
            if (timeStamp != newTimeStamp) {
                System.out.println("xxxxxxxxxxxx originTimeStamp: " + timeStamp + " newTimeStamp: "
                    + newTimeStamp);
            }
            return newTimeStamp;
        }else{
            return timeStamp;
        }
    }
}
