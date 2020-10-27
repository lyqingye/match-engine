package com.trader.core;

import com.trader.entity.Order;

/**
 * @author yjt
 * @since 2020/10/23 上午9:27
 */
public interface Processor {


    /**
     * 执行订单
     *
     * @param order
     *         订单
     */
    void exec(Order order);

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    String name();

    /**
     * 修改处理器的名称
     *
     * @param newName
     *         新的名字
     */
    void renaming(String newName);
}
