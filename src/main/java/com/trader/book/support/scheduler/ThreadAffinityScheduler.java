package com.trader.book.support.scheduler;

import com.trader.book.Scheduler;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;

/**
 * 基于线程亲和性调度器
 * <p>
 * https://github.com/peter-lawrey/Java-Thread-Affinity/wiki/Getting-started
 *
 * @author yjt
 * @since 2020/10/23 上午10:03
 */
public class ThreadAffinityScheduler implements Scheduler {
    /**
     * 提交需要调度的订单及订单簿
     *
     * @param order
     *         订单
     * @param book
     *         订单簿
     */
    @Override
    public void submit(Order order, OrderBook book) {

    }
}
