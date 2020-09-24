package com.trader;

import com.trader.context.MatchingContext;
import com.trader.context.ThreadLocalMatchingContext;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;
import com.trader.matcher.TradeResult;
import com.trader.utils.ThreadLocalUtils;

import java.util.Objects;

/**
 * 撮合引擎事件处理器
 *
 * @author yjt
 * @since 2020/9/1 上午10:25
 */
public interface MatchHandler {

    /**
     * 优先级,优先级越高越先执行
     *
     * @return 优先级
     */
    default int getPriority () {
        return Integer.MIN_VALUE;
    }

    /**
     * 商品添加事件处理器
     *
     * @param product 商品
     * @throws Exception
     */
    default void onAddProduct (Product product) throws Exception {}

    /**
     * 商品事件处理器
     *
     * @param product 商品
     * @throws Exception
     */
    default void onDelProduct (Product product) throws Exception {}

    /**
     * 委托账本添加事件
     *
     * @param book 账本
     * @throws Exception
     */
    default void onAddOrderBook (OrderBook book) throws Exception {}

    /**
     * 委托账本删除事件
     *
     * @param book 账本
     * @throws Exception
     */
    default void onDelOrderBook (OrderBook book) throws Exception {}

    /**
     * 添加订单事件
     *
     * @param newOrder 订单
     * @throws Exception
     */
    default void onAddOrder (Order newOrder) throws Exception {}

    /**
     * 激活止盈止损订单事件
     *
     * @param stopOrder 止盈止损订单
     * @throws Exception
     */
    default void activeStopOrder(Order stopOrder) throws Exception {}

    /**
     * 更新订单事件
     *
     * @param order 订单
     * @throws Exception
     */
    default void onUpdateOrder (Order order) throws Exception {}

    /**
     * 删除订单事件
     *
     * @param order 订单
     * @throws Exception
     */
    default void onDelOrder (Order order) throws Exception {}

    /**
     * 撮合订单事件
     *
     * @param order 订单
     * @param opponentOrder 对手订单
     * @param ts 撮合结果
     * @throws Exception
     */
    default void onExecuteOrder(Order order, Order opponentOrder, TradeResult ts) throws Exception {}

    /**
     * 获取上下文
     *
     * @return 上下文对象
     */
    default MatchingContext ctx () {
        return Objects.requireNonNull(ThreadLocalUtils.get(ThreadLocalMatchingContext.NAME_OF_CONTEXT),
                                      "无法获取上下文");
    }
}
