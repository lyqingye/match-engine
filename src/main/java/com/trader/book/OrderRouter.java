package com.trader.book;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

/**
 * 订单路由, 主要职责是负责将所给的订单映射到指定的订单簿
 *
 * @author yjt
 * @since 2020/10/23 上午8:26
 */
public interface OrderRouter {

    /**
     * 路由的名字
     *
     * @return 路由的名字
     */
    String name();

    /**
     * 路由的描述
     *
     * @return 路由的描述
     */
    String desc();

    /**
     * 根据所给的订单信息, 映射到指定的订单簿
     *
     * @param order
     *         订单
     *
     * @return 订单簿 or null 如果没有合适的订单
     */
    OrderBook mapTo(Order order);
}
