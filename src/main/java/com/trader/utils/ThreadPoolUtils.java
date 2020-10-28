package com.trader.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yjt
 * @since 2020/9/24 上午9:29
 */
public class ThreadPoolUtils {

    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newFixedThreadPool(2,
                                                        new ThreadFactory() {
                                                            AtomicInteger counter = new AtomicInteger(0);

                                                            @Override
                                                            public Thread newThread(Runnable r) {
                                                                final Thread thr = new Thread(r);
                                                                thr.setName("Match-Thread-Pool:" + counter.getAndIncrement());
                                                                return thr;
                                                            }
                                                        });
    }

    public static void submit(Runnable runnable) {
        EXECUTOR_SERVICE.submit(runnable);
    }
}
