package com.trader.matcher.limit;

import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.helper.TradeHelper;
import com.trader.matcher.TradeResult;

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

        //
        // 处理以下类型的订单
        //
        // STOP <-> LIMIT   （止盈止损单和限价单）
        // LIMIT <-> LIMIT  （限价单和限价单）
        //

        if (order.isMarketOrder() || opponentOrder.isMarketOrder()) {
            return false;
        }

        // 不允许自身撮合
        if (order.getUid().equals(opponentOrder.getUid())) {
            return false;
        }

        BigDecimal price = order.getBoundPrice();
        BigDecimal opponentPrice = opponentOrder.getBoundPrice();

        //
        // 区分买卖单:
        // 买入单: 则卖盘的价格必须要 <= 买入价
        // 卖出单: 则买盘的价格必须要 >= 卖出价
        //
        boolean arbitrage;
        if (order.isBuy()) {
            arbitrage = opponentPrice.compareTo(price) <= 0;
        } else {
            arbitrage = opponentPrice.compareTo(price) >= 0;
        }

        if (!arbitrage)
            return false;

        //
        // 判断是否有足够的钱进行购买
        //
        if (!TradeHelper.isHasEnoughAmount(order, opponentPrice) ||
                !TradeHelper.isHasEnoughAmount(opponentOrder, price)) {
            return false;
        }

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
        return TradeHelper.genericTrade(order, opponentOrder, BigDecimal.ZERO);
    }
}
