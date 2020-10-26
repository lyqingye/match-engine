package com.trader.book.support.processor;

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
            tr.setName("match-processor:" + newName);
        }
    }

    @Override
    public Thread newThread(Runnable var1) {
        String trName = "match-processor:" + name;
        Thread var2 = new Thread(this.group, var1, trName, 0L);
        if (var2.isDaemon()) {
            var2.setDaemon(false);
        }

        if (var2.getPriority() != 5) {
            var2.setPriority(5);
        }

        threadMap.put(this.name, var2);
        return var2;
    }
}
