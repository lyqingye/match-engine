package com.trader.core.matcher.market;

import com.trader.core.MatchHandler;
import com.trader.core.def.OrderType;
import com.trader.core.entity.Order;
import com.trader.core.matcher.MatchResult;

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
     * 扣除订单
     *
     * @param order
     *         订单
     * @param executeQuantity
     *         成交量
     */
    private static void updateOrder(Order order,
                                   BigDecimal executeQuantity,
                                   BigDecimal amount) {
        // 如果买入单则扣除成交总金额
        if (order.getType().equals(OrderType.MARKET)) {
            if (order.isBuy()) {
                order.incExecutedAmount(amount);
                order.decLeavesAmount(amount);

                // 记录已经成交的数量
                order.incExecutedQuality(executeQuantity);
            }

            // 如果是卖出单则扣除成交量
            if (order.isSell()) {
                order.incExecutedQuality(executeQuantity);
                order.decLeavesQuality(executeQuantity);

                // 记录已经获得的金额
                order.incExecutedAmount(amount);
            }
        }
    }

    /**
     * 撮合订单事件
     *
     * @param order
     *         当前订单
     * @param opponentOrder
     *         对手订单
     * @param ts
     *         撮合结果
     *
     * @throws Exception
     *         如果撮合失败
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, MatchResult ts) throws Exception {
        BigDecimal quantity = ts.getQuantity();

        //
        // 处理以下类型的订单
        //
        // MARKET <-> LIMIT （市价单和限价单）
        // MARKET <-> MARKET  （市价单和市价单）
        // MARKET <-> STOP (市价单和止盈止损单)

        if (order.getType().equals(OrderType.MARKET) || opponentOrder.getType().equals(OrderType.MARKET)) {
            InMemoryMarketMatchHandler.updateOrder(order, quantity, ts.getExecuteAmount());
            InMemoryMarketMatchHandler.updateOrder(opponentOrder, quantity, ts.getOpponentExecuteAmount());
        }
    }
}
