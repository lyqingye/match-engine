package com.trader.matcher.limit;

import com.trader.MatchHandler;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author yjt
 * @since 2020/9/17 下午7:00
 */
public class InMemoryLimitMatchHandler implements MatchHandler {
    /**
     * 撮合订单事件
     *
     * @param order
     * @param opponentOrder
     * @param ts
     *
     * @throws Exception
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, TradeResult ts) throws Exception {

        switch (order.getType()) {
            case STOP:
            case LIMIT: {

                if (order.isBuy()) {
                    // 限价买单
                    BigDecimal executeAmount = ts.getExecutePrice()
                                                 .multiply(ts.getQuantity())
                                                 .setScale(8, RoundingMode.DOWN);
                    order.decLeavesAmount(executeAmount);
                    order.incExecutedAmount(executeAmount);
                    order.incExecutedQuality(ts.getQuantity());
                } else {
                    BigDecimal quantity = ts.getQuantity();
                    //
                    // 增加成交量, 与减少剩余成交量
                    //
                    order.incExecutedQuality(quantity);
                    order.decLeavesQuality(quantity);
                }

                if (opponentOrder.isBuy()) {
                    // 限价买单
                    BigDecimal executeAmount = ts.getOpponentExecutePrice()
                                                 .multiply(ts.getQuantity())
                                                 .setScale(8, RoundingMode.DOWN);
                    opponentOrder.decLeavesAmount(executeAmount);
                    opponentOrder.incExecutedAmount(executeAmount);
                    opponentOrder.incExecutedQuality(ts.getQuantity());
                } else {
                    BigDecimal quantity = ts.getQuantity();
                    //
                    // 增加成交量, 与减少剩余成交量
                    //
                    opponentOrder.incExecutedQuality(quantity);
                    opponentOrder.decLeavesQuality(quantity);
                }
                break;
            }
            default: {
                // ignored
            }
        }
    }
}
