package com.trader.book.support.processor;

import com.trader.book.OrderRouter;
import com.trader.entity.OrderBook;
import com.trader.support.OrderBookManager;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yjt
 * @since 2020/10/23 下午1:30
 */
public class ProcessorThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String name;

    ProcessorThreadFactory(String name) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.name = "match-processor:" + name;
    }

    @Override
    public Thread newThread(Runnable var1) {
        Thread var2 = new Thread(this.group, var1, this.name, 0L);
        if (var2.isDaemon()) {
            var2.setDaemon(false);
        }

        if (var2.getPriority() != 5) {
            var2.setPriority(5);
        }
        return var2;
    }
}
