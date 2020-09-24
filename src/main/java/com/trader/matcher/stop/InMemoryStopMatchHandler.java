package com.trader.matcher.stop;

import com.trader.MatchHandler;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;

/**
 * @author yjt
 * @since 2020/9/24 上午10:30
 */
public class InMemoryStopMatchHandler implements MatchHandler {

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
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder,
                               TradeResult ts) throws Exception {

    }
}
