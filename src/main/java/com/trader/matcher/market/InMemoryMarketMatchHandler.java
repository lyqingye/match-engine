package com.trader.matcher.market;

import com.trader.MatchHandler;
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
        
    }
}
