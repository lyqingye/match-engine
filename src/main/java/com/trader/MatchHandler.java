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
