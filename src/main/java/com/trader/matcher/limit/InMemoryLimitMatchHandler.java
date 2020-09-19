package com.trader.matcher.limit;

import com.trader.MatchHandler;
import com.trader.entity.Order;
import java.math.BigDecimal;

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

        switch (order.getType()) {
            case STOP:
            case LIMIT: {
                //
                // 增加成交量, 与减少剩余成交量
                //
                order.incExecutedQuality(quantity);
                order.decLeavesQuality(quantity);
                opponentOrder.incExecutedQuality(quantity);
                opponentOrder.decLeavesQuality(quantity);
                break;
            }
            default: {
                // ignored
            }
        }
    }
}
