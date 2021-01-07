package com.trader.core;

import com.trader.core.context.MatchingContext;
import com.trader.core.entity.Order;
import com.trader.core.matcher.MatchResult;
import com.trader.utils.TradeUtils;

/**
 * @author yjt
 * @since 2020/9/18 上午9:12
 */
public interface Matcher {

    /**
     * 判断是否支持目标订单的匹配
     *
     * @param order
     *         当前订单
     * @param opponentOrder
     *         对手订单
     * @param ctx
     *         上下文
     * @return 是否支持匹配
     */
    boolean isSupport(Order order, Order opponentOrder, MatchingContext ctx);

    /**
     * 进行撮合交易
     *
     * @param order
     *         当前订单
     * @param opponentOrder
     *         对手订单
     *
     * @param ctx
     *         上下文
     * @return 交易结果
     */
    MatchResult doTrade(Order order, Order opponentOrder, MatchingContext ctx);

    /**
     * 目标订单是否已经结束
     *
     * @param order
     *         order
     *
     * @return 是否已经结束
     */
    default boolean isFinished(Order order) {
        return TradeUtils.isFinished(order);
    }
}
