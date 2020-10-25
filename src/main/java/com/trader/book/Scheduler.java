package com.trader.book;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

/**
 * 负责调度 {@link Processor}
 *
 * @author yjt
 * @since 2020/10/23 上午9:47
 */
public interface Scheduler {

    /**
     * 提交需要调度的订单
     *
     * @param order
     *         订单
     */
    void submit(Order order);
}
