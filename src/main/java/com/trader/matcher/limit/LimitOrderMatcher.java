package com.trader.matcher.limit;

import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;
import com.trader.utils.MathUtils;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 上午9:16
 */
public class LimitOrderMatcher implements Matcher {
    /**
     * 判断是否支持目标订单的匹配
     *
     * @param order
     *         当前订单
     * @param opponentOrder
     *         对手订单
     *
     * @return 是否支持匹配
     */
    @Override
    public boolean isSupport(Order order, Order opponentOrder) {

        if (!order.getType().equals(OrderType.LIMIT) ||
                !opponentOrder.getType().equals(OrderType.LIMIT)) {
            return false;
        }

        // TODO 允许自身撮合
//        if (order.getUid().equals(opponentOrder.getUid())) {
//            return false;
//        }

        //
        // 区分买卖单:
        // 买入单: 则买入的价格必须要 <= 卖盘
        // 卖出单: 则卖出的价格必须要 >= 买盘
        //
        boolean arbitrage;
        if (order.isBuy()) {
            arbitrage = order.getPrice().compareTo(opponentOrder.getPrice()) <= 0;
        } else {
            arbitrage = opponentOrder.getPrice().compareTo(order.getPrice()) >= 0;
        }

        if (!arbitrage)
            return false;

        //
        // TODO 处理全量交易
        // + 全部执行或取消 (Fill ok kill)
        // + 一次性全部执行或不执行 (All or none)
        //
        if (order.isAON() || order.isFOK()) {
            return false;
        }

        //
        // 全量交易订单
        // 如果对手盘是全量交易订单,则需要判断成交量是否 > 目标订单的剩余量
        //
        if (opponentOrder.isAON()) {
            if (opponentOrder.getLeavesQuantity().compareTo(order.getLeavesQuantity()) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 进行撮合交易
     *
     * @param order
     *         当前订单
     * @param opponentOrder
     *         对手订单
     *
     * @return 交易结果
     */
    @Override
    public TradeResult doTrade(Order order, Order opponentOrder) {
        // 计算成交量
        BigDecimal quantity = MathUtils.min(order.getLeavesQuantity(),
                                            opponentOrder.getLeavesQuantity());
        // 成交价
        BigDecimal price = opponentOrder.getPrice();
        return new TradeResult(price,quantity);
    }

    /**
     * 目标订单是否已经结束
     *
     * @param order
     *         order
     *
     * @return 是否已经结束
     */
    @Override
    public boolean isFinished(Order order) {
        return order.getLeavesQuantity().compareTo(BigDecimal.ZERO) == 0;
    }
}
