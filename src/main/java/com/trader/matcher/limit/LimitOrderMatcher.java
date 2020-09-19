package com.trader.matcher.limit;

import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;
import com.trader.utils.MathUtils;

import java.math.BigDecimal;

/**
 * 限价订单匹配器
 *
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
        // 买入单: 则卖盘的价格必须要 <= 买入价
        // 卖出单: 则买盘的价格必须要 >= 卖出价
        //
        boolean arbitrage;
        if (order.isBuy()) {
            arbitrage = opponentOrder.getPrice().compareTo(order.getPrice()) <= 0;
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

        //
        // 平台要吃掉差价. 并且这个过程对于用户来说是透明的.
        // 例：
        // 买家以 10块单价买入BTC 10个
        // 卖家以 9块单价卖出BTC 10个
        // 所以对于买家来说, 成交价是 10 块
        // 卖家的成交价是: 9块
        // NOTE 这样的话用户根本赚不了钱, 真的🐮🍺

        BigDecimal executePrice = BigDecimal.ZERO;
        if (order.isBuy()) {
            executePrice = order.getPrice();
        }

        if (order.isSell()) {
            executePrice = opponentOrder.getPrice();
        }

        // TODO 也顺便记录下真实的成交价格, 也就是对手盘的价格
        BigDecimal actualExecutePrice = opponentOrder.getPrice();

        return new TradeResult(executePrice,quantity);
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
