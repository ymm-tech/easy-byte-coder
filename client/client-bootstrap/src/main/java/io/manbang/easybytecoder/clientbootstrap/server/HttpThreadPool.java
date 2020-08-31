package io.manbang.easybytecoder.clientbootstrap.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpThreadPool {

    public static ExecutorService httpServerPool = new ThreadPoolExecutor(5, 5,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(20), new ThreadPoolExecutor.AbortPolicy());

}
