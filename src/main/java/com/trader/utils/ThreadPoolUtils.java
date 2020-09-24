package com.trader.utils;

import java.util.concurrent.*;

/**
 * @author yjt
 * @since 2020/9/24 上午9:29
 */
public class ThreadPoolUtils {

    private static final ExecutorService EXECUTOR_SERVICE ;

    static {
        EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
    }

    public static void submit (Runnable runnable) {
        EXECUTOR_SERVICE.submit(runnable);
    }
}
