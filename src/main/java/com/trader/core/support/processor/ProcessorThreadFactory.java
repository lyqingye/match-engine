package com.trader.core.support.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yjt
 * @since 2020/10/23 下午1:30
 */
public class ProcessorThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String name;
    private static AtomicInteger counter = new AtomicInteger(0);
    private Map<String, Thread> threadMap = new HashMap<>();

    ProcessorThreadFactory(String name) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.name = name;
    }

    public void rename(String name, String newName) {
        if (threadMap.containsKey(name)) {
            Thread tr = threadMap.get(name);
            tr.setName("Match-Processor: " + newName);
            threadMap.remove(name);
            threadMap.put(newName, tr);
        }
    }

    @Override
    public Thread newThread(Runnable cmd) {
        String trName = "Match-Processor: " + name;
        Thread tr = new Thread(this.group, cmd, trName, 0L);
        if (tr.isDaemon()) {
            tr.setDaemon(false);
        }

        if (tr.getPriority() != 5) {
            tr.setPriority(5);
        }

        threadMap.put(this.name, tr);
        tr.setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("[MatchProcessor]: uncaught exception in thread: " + thread.getName());
            throwable.printStackTrace();
        });
        return tr;
    }
}
