package com.trader.matcher.market;

import com.trader.MatchHandler;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;

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
     * @param ts
     * @throws Exception
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, TradeResult ts) throws Exception {
        BigDecimal quantity = ts.getQuantity();

        //
        // 处理以下类型的订单
        //
        // MARKET <-> LIMIT （市价单和限价单）
        // MARKET <-> MARKET  （市价单和市价单）
        // MARKET <-> STOP (市价单和止盈止损单)

        if (order.getType().equals(OrderType.MARKET) || opponentOrder.getType().equals(OrderType.MARKET)) {
            this.processOrder(order, ts.getExecutePrice(), quantity);
            this.processOrder(opponentOrder, ts.getExecutePrice(), quantity);
        }
    }

    /**
     * 扣除订单
     *
     * @param order 订单
     * @param price 成交价
     * @param quantity 成交量
     */
    private void processOrder(Order order, BigDecimal price, BigDecimal quantity) {

        switch (order.getType()) {
            case LIMIT:
            case STOP: {
                /**
                 * 如果该订单不是市价单
                 * 比如订单是: {@link OrderType#LIMIT] 或者 {@link OrderType#STOP}
                 * 那么就直接扣除成交量即可
                 */
                order.incExecutedQuality(quantity);
                order.decLeavesQuality(quantity);
                break;
            }
            case MARKET: {
                // 计算成交总金额 = 单价 * 成交量
                BigDecimal totalAmount = price.multiply(quantity);

                // 如果买入单则扣除成交总金额
                if (order.isBuy()) {
                    order.incExecutedAmount(totalAmount);
                    order.decLeavesAmount(totalAmount);
                }

                // 如果是卖出单则扣除成交量
                if (order.isSell()) {
                    order.incExecutedQuality(quantity);
                    order.decLeavesQuality(quantity);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("不支持该订单类型与市价单交易");
            }
        }
    }
}
