package com.trader;

import com.trader.context.MatchingContext;
import com.trader.context.ThreadLocalMatchingContext;
import com.trader.entity.Order;
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
    default int getPriority() {
        return Integer.MIN_VALUE;
    }


    /**
     * 添加订单事件
     *
     * @param newOrder
     *         订单
     *
     * @throws Exception
     */
    default void onAddOrder(Order newOrder) throws Exception {
    }

    /**
     * 激活止盈止损订单事件
     *
     * @param stopOrder
     *         止盈止损订单
     *
     * @throws Exception
     */
    default void onActiveStopOrder(Order stopOrder) throws Exception {
    }


    /**
     * 订单移除事件
     * 警告: 当发生异常不会导致订单回滚, 为什么这么做呢,我来解释下
     * <p>
     * 因为撮合是单线程的, 订单移除可能是异步的, 这就会导致数据并发
     * 所以在调用取消订单的时候, 我只是对这个订单进行了标记
     * 当撮合引擎匹配到这个被标记为取消的订单后, 将不会被撮合 并且会调用
     * 当前事件, 所以这个事件不会涉及到持久化操作,所以不应该抛出异常
     *
     * @param removed
     *         已经被移除的事件
     */
    default void onOrderCancel(Order removed) {

    }


    /**
     * 撮合订单事件
     *
     * @param order
     *         订单
     * @param opponentOrder
     *         对手订单
     * @param ts
     *         撮合结果
     *
     * @throws Exception
     *         如果发生异常
     */
    default void onExecuteOrder(Order order, Order opponentOrder, TradeResult ts) throws Exception {
    }

    /**
     * 获取上下文
     *
     * @return 上下文对象
     */
    default MatchingContext ctx() {
        return Objects.requireNonNull(ThreadLocalUtils.get(ThreadLocalMatchingContext.NAME_OF_CONTEXT),
                                      "无法获取上下文");
    }
}
