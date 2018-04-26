package com.hzj.captureservice.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;

/**
 * <p>Thread executor.</p>
 * Created by Yan Zhenjie on 2016/6/13.
 */
public class Executors {

    private int MAX_THREAD_COUNT=256;

    private static Executors instance;

    /**
     * Get instance.
     *
     * @return {@link Executors}.
     */
    public static Executors getInstance() {
        if (instance == null)
            synchronized (Executors.class) {
                if (instance == null)
                    instance = new Executors();
            }
        return instance;
    }

    /**
     * Executor Service.
     */
    private final ExecutorService mService;

    /**
     * Handler.
     */
    private static Handler mHandler;

    private Executors() {
        mService = java.util.concurrent.Executors.newFixedThreadPool (MAX_THREAD_COUNT);
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Execute a runnable.
     *
     * @param command {@link Runnable}.
     */
    public void executorService(Runnable command) {
        mService.execute(command);
    }

    /**
     * Execute a runnable.
     *
     * @param command {@link Runnable}.
     */
    public void handler(Runnable command) {
        mHandler.post(command);
    }
}
