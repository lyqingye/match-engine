package com.trader;

import com.trader.context.MatchingContext;
import com.trader.entity.Order;
import com.trader.helper.TradeHelper;
import com.trader.matcher.TradeResult;

/**
 * @author yjt
 * @since 2020/9/18 上午9:12
 */
public interface Matcher {

    /**
     * 判断是否支持目标订单的匹配
     *
     * @param order 当前订单
     * @param opponentOrder 对手订单
     * @return 是否支持匹配
     */
    boolean isSupport (Order order, Order opponentOrder);

    /**
     * 进行撮合交易
     *
     * @param order 当前订单
     * @param opponentOrder 对手订单
     * @return 交易结果
     */
    TradeResult doTrade (Order order, Order opponentOrder);

    /**
     * 目标订单是否已经结束
     *
     * @param order order
     * @return 是否已经结束
     */
    default boolean isFinished (Order order) {
        return TradeHelper.isFinished(order);
    }

    /**
     * 获取上下文
     *
     * @return 上下文对象
     */
    default MatchingContext ctx () {throw new UnsupportedOperationException("无上下文");}
}
