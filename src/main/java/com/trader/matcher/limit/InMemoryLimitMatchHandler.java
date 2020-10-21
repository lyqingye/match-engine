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
     *
     * @throws Exception
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, TradeResult ts) throws Exception {
        InMemoryLimitMatchHandler.executeOrder(order, ts, ts.getExecuteAmount());
        InMemoryLimitMatchHandler.executeOrder(opponentOrder, ts, ts.getOpponentExecuteAmount());
    }

    /**
     * 处理限价和止盈止损单
     * 根据撮合结果,更新订单数据
     *
     * @param order
     *         订单
     * @param ts
     *         撮合结果
     */
    public static void executeOrder(Order order, TradeResult ts, BigDecimal amount) {

        switch (order.getType()) {
            case STOP:
            case LIMIT: {
                InMemoryLimitMatchHandler.updateOrder(order, ts.getQuantity(), amount);
                break;
            }
            default: {
                // ignored
            }
        }
    }

    /**
     * 更新订单
     *
     * @param order
     *         订单
     * @param executePrice
     *         成交价格
     * @param executeQuantity
     *         成交数量
     */
    public static void updateOrder(Order order,
                                   BigDecimal executeQuantity,
                                   BigDecimal amount) {
        if (order.isBuy()) {
            // 限价买单

            order.decLeavesAmount(amount);
            order.incExecutedAmount(amount);

            // 买单需要记录已经获得的数量
            order.incExecutedQuality(executeQuantity);
        } else {
            //
            // 增加成交量, 与减少剩余成交量
            //
            order.incExecutedQuality(executeQuantity);
            order.decLeavesQuality(executeQuantity);

            // 卖单需要记录已经获得的金钱
            order.incExecutedAmount(amount);
        }
    }
}
