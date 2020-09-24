package com.trader.matcher.market;

import com.trader.MatchHandler;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;
import com.trader.matcher.limit.InMemoryLimitMatchHandler;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午4:23
 */
public class InMemoryMarketMatchHandler implements MatchHandler {

    /**
     * 优先级,优先级越高越先执行
     *
     * @return 优先级
     */
    @Override
    public int getPriority() {

        // 内存撮合优先级最高
        return Integer.MAX_VALUE;
    }

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
            InMemoryMarketMatchHandler.updateOrder(order, ts.getExecutePrice(), quantity);
            InMemoryMarketMatchHandler.updateOrder(opponentOrder, ts.getOpponentExecutePrice(), quantity);
        }
    }

    /**
     * 扣除订单
     *
     * @param order 订单
     * @param executePrice 成交价
     * @param executeQuantity 成交量
     */
    public static void updateOrder(Order order,
                                   BigDecimal executePrice,
                                   BigDecimal executeQuantity) {

        switch (order.getType()) {
            case LIMIT:
            case STOP: {

                // 处理限价和止盈止损订单
                InMemoryLimitMatchHandler.updateOrder(order, executePrice, executeQuantity);
                break;
            }
            case MARKET: {
                // 计算成交总金额 = 单价 * 成交量
                BigDecimal totalAmount = executePrice.multiply(executeQuantity);

                // 如果买入单则扣除成交总金额
                if (order.isBuy()) {
                    order.incExecutedAmount(totalAmount);
                    order.decLeavesAmount(totalAmount);
                }

                // 如果是卖出单则扣除成交量
                if (order.isSell()) {
                    order.incExecutedQuality(executeQuantity);
                    order.decLeavesQuality(executeQuantity);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("不支持该订单类型与市价单交易");
            }
        }
    }
}
