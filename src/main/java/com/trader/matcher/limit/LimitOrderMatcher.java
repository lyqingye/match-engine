package com.trader.matcher.limit;

import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.helper.TradeHelper;
import com.trader.matcher.TradeResult;
import com.trader.utils.MathUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        if (!TradeHelper.isHasEnoughAmount(order,opponentPrice) ||
                !TradeHelper.isHasEnoughAmount(opponentOrder,price)) {
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

        //
        // 计算成交价
        //
        TradeResult ts = TradeHelper.calcExecutePrice(order,
                                                      opponentOrder,
                                                      null);
        BigDecimal executePrice = ts.getExecutePrice();
        BigDecimal opponentExecutePrice = ts.getOpponentExecutePrice();

        //
        // 计算最终成交量
        //
        BigDecimal quantity = order.getLeavesQuantity();
        BigDecimal opponentQuantity = opponentOrder.getLeavesQuantity();

        //
        // 如果是买入单, 则需要用待执行金额 / 成交价 = 待执行数量
        //
        if (order.isBuy()) {
            quantity = order.getLeavesAmount()
                            .divide(executePrice, RoundingMode.DOWN);
        }

        if (opponentOrder.isBuy()) {
            opponentQuantity = opponentOrder.getLeavesAmount()
                                            .divide(opponentExecutePrice, RoundingMode.DOWN);
        }

        // 成交量取两者最少部分
        BigDecimal executeQuantity = MathUtils.min(quantity,
                                                   opponentQuantity);
        ts.setQuantity(executeQuantity);
        return ts;
    }
}
