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
        // 如果买家的钱连 0.00000001 都买不起那就直接无法撮合, 因为成交数量不能为空
        //
        if (order.isBuy()) {
            if (order.getLeavesAmount().divide(opponentPrice, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
        }
        if (opponentOrder.isBuy()) {
            if (opponentOrder.getLeavesAmount().divide(price, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
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
        // 平台要吃掉差价. 并且这个过程对于用户来说是透明的.
        // 例：
        // 买家以 10块单价买入BTC 10个
        // 卖家以 9块单价卖出BTC 10个
        // 所以对于买家来说, 成交价是 10 块
        // 卖家的成交价是: 9块
        // 🐮🍺

        TradeResult ts = TradeHelper.calcExecutePrice(order,
                                                      opponentOrder,
                                                      null);

        // 计算当前订单最终成交价
        BigDecimal executePrice = ts.getExecutePrice();

        // 计算目标订单最终成交价
        BigDecimal opponentExecutePrice = ts.getOpponentExecutePrice();

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

        // 计算成交量
        BigDecimal executeQuantity = MathUtils.min(quantity,
                                                   opponentQuantity);

        ts.setQuantity(executeQuantity);
        return ts;
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
        if (order.isBuy()) {
            return order.getLeavesAmount().compareTo(BigDecimal.ZERO) == 0;
        }
        return order.getLeavesQuantity().compareTo(BigDecimal.ZERO) == 0;
    }
}
