package com.chinatsp.ifly.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {

    private static final int DEFAULT_CORE_SIZE = 5;
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final int DEFAULT_QUEUE_SIZE = 5;
    private static final int DEFAULT_KEEPALIVE_TIME = 0;

    private static volatile ThreadPoolExecutor mThreadPool;
    private static volatile ScheduledThreadPoolExecutor mScheduledPool;


    private static void createThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadPoolUtils.class) {
                if (mThreadPool == null) {
                    mThreadPool = new ThreadPoolExecutor(DEFAULT_CORE_SIZE, DEFAULT_MAX_SIZE,
                            DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS,
                            new ArrayBlockingQueue(DEFAULT_QUEUE_SIZE),
                            new ThreadPoolExecutor.DiscardOldestPolicy());
                }
                if(mScheduledPool==null){
                    mScheduledPool = new ScheduledThreadPoolExecutor(DEFAULT_CORE_SIZE);
                }
            }
        }
    }

    public static void execute(Runnable runnable) {
        if (mThreadPool == null) {
            createThreadPool();
        }
        mThreadPool.execute(runnable);
    }

    private static ExecutorService executorService;
    public static void executeSingle(Runnable runnable) {
        if(executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.execute(runnable);
    }

    public static void schheduleRunnale(Runnable runnable,int time){
        if (mScheduledPool == null) {
            createThreadPool();
        }
        if(mScheduledPool!=null)
            mScheduledPool.schedule(runnable,time,TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        if (mThreadPool == null) {
            return;
        }
        mThreadPool.shutdown();

        if(executorService == null) {
            return;
        }
        executorService.shutdown();
    }

}
