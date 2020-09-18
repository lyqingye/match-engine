package com.trader.matcher.market;

import com.trader.MarketManager;
import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;
import com.trader.utils.MathUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author yjt
 * @since 2020/9/18 下午2:47
 */
public class MarketOrderMatcher implements Matcher {
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
        if (!(order.getType().equals(OrderType.MARKET) ||
                opponentOrder.getType().equals(OrderType.MARKET))) {
            return false;
        }
        MarketManager marketMgr = this.ctx().getMarketMgr();
        BigDecimal marketPrice = BigDecimal.ZERO;

        BigDecimal orderPrice = order.getType().equals(OrderType.MARKET) ?  marketPrice : order.getPrice();
        BigDecimal opponentPrice = opponentOrder.getType().equals(OrderType.MARKET) ?  marketPrice : opponentOrder.getPrice();

        boolean arbitrage;
        if (order.isBuy()) {
            arbitrage = orderPrice.compareTo(opponentPrice) <= 0;
        } else {
            arbitrage = opponentPrice.compareTo(orderPrice) >= 0;
        }

        if (!arbitrage)
            return false;

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
        BigDecimal leavesQuality = order.getLeavesQuantity();
        BigDecimal opponentLeavesQuality = opponentOrder.getLeavesQuantity();
        MarketManager marketMgr = this.ctx().getMarketMgr();
        BigDecimal marketPrice = BigDecimal.ZERO;

        if (order.getType().equals(OrderType.MARKET)) {
            leavesQuality = order.getLeavesAmount().divide(marketPrice, RoundingMode.DOWN);
        }

        if (opponentOrder.getType().equals(OrderType.MARKET)) {
            opponentLeavesQuality = opponentOrder.getLeavesAmount().divide(marketPrice, RoundingMode.DOWN);
        }

        // 计算成交量
        BigDecimal quantity = MathUtils.min(leavesQuality,
                                            opponentLeavesQuality);
        // 成交价
        BigDecimal price = opponentOrder.getType().equals(OrderType.MARKET) ? marketPrice : opponentOrder.getPrice();
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
        return order.getLeavesAmount().compareTo(BigDecimal.ZERO) == 0;
    }
}
