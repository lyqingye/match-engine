package com.trader.matcher.market;

import com.trader.Matcher;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.helper.TradeHelper;
import com.trader.market.MarketManager;
import com.trader.matcher.TradeResult;

import java.math.BigDecimal;

/**
 * 市价订单匹配器
 *
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

        // 不允许自身撮合
        if (order.getUid().equals(opponentOrder.getUid())) {
            return false;
        }

        MarketManager marketMgr = this.ctx().getMarketMgr();
        BigDecimal marketPrice = marketMgr.getMarketPrice(order);

        //
        // 支持以下类型的订单进行撮合
        //
        // MARKET <-> LIMIT （市价单和限价单）
        // MARKET <-> MARKET  （市价单和市价单）
        // MARKET <-> STOP (市价单和止盈止损单)

        //
        // 市价订单:
        // 买: 单价 = 市场价, 执行量 = 交易额 / 单价
        // 卖: 单价 = 市场价
        //
        /** 撮合条件判定只需要判定双方买卖价格即可, 参考限价交易 {@link com.trader.matcher.limit.LimitOrderMatcher}*/

        BigDecimal price = order.isMarketOrder() ? marketPrice : order.getBoundPrice();
        BigDecimal opponentPrice = opponentOrder.isMarketOrder() ? marketPrice : opponentOrder.getBoundPrice();

        //
        // 判断是否有足够的钱进行购买
        //
        if (!TradeHelper.isHasEnoughAmount(order, opponentPrice) ||
                !TradeHelper.isHasEnoughAmount(opponentOrder, price)) {
            return false;
        }


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

        return arbitrage;
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
        MarketManager marketMgr = this.ctx().getMarketMgr();
        BigDecimal marketPrice = marketMgr.getMarketPrice(order);
        return TradeHelper.genericTrade(order, opponentOrder, marketPrice);
    }
}
