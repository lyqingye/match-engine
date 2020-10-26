package com.trader.book;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

import java.util.Collection;

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
    OrderBook routeTo(Order order);


    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要触发止盈止损订单的订单簿
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿集合
     */
    Collection<OrderBook> routeToNeedToActiveBook(String symbolId);

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要更新最新成交价的订单簿
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿集合
     */
    Collection<OrderBook> routeToNeedToUpdatePriceBook(String symbolId);

    /**
     * 给定一个交易对, 返回一个订单簿, 用于第三方调用者查询市场价
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿
     */
    OrderBook routeToBookForQueryPrice(String symbolId);

    /**
     * 给定一个订单, 返回该订单实际撮合获取市价的订单簿
     * 用于撮合过程中, 如果该订单为市价单, 则从该订单簿获取最新成交价
     *
     * @param order
     *         订单
     *
     * @return 订单簿
     */
    OrderBook routeToBookForQueryPrice(Order order);

    /**
     * 给定一个订单, 返回一个订单簿, 当该订单引起盘口变化时
     * 需要根据该订单获取需要发送的盘口
     *
     * @param order
     *         订单
     *
     * @return 订单簿
     */
    OrderBook routeToBookForSendDepthChart(Order order);
}
