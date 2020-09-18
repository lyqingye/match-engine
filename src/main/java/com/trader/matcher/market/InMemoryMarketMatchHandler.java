package com.trader.matcher.market;

import com.trader.MatchHandler;
import com.trader.def.OrderType;
import com.trader.entity.Order;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午4:23
 */
public class InMemoryMarketMatchHandler implements MatchHandler {

    /**
     * 撮合订单事件
     *
     * @param order
     * @param opponentOrder
     * @param price
     * @param quantity
     *
     * @throws Exception
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder,
                               BigDecimal price,
                               BigDecimal quantity) throws Exception {
        BigDecimal amount = price.multiply(quantity);

        if (order.getType().equals(OrderType.MARKET)) {
            order.incExecutedAmount(amount);
            order.decLeavesAmount(amount);
        } else {
            order.incExecutedQuality(quantity);
            order.decLeavesQuality(quantity);
        }

        if (opponentOrder.getType().equals(OrderType.MARKET)) {
            opponentOrder.incExecutedAmount(amount);
            opponentOrder.decLeavesAmount(amount);
        }else {
            opponentOrder.incExecutedQuality(quantity);
            opponentOrder.decLeavesQuality(quantity);
        }
    }
}
