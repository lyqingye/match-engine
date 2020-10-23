package com.trader.book.support.scheduler;

import com.trader.book.Scheduler;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;

/**
 * 单线程调度器
 *
 * @author yjt
 * @since 2020/10/23 上午10:02
 */
public class SingleThreadScheduler implements Scheduler {
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
