package com.trader.core;

import com.trader.core.entity.Order;

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
     * @param order 订单
     */
    void submit(Order order);

    /**
     * 调度器销毁
     */
    void shutdownAndWait();
}
