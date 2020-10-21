package com.trader.matcher.market;

import com.trader.MatchEngine;
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
        MatchEngine me = ctx().getEngine();

        if (!(order.getType().equals(OrderType.MARKET) ||
                opponentOrder.getType().equals(OrderType.MARKET))) {

            if (me.isEnableLog()) {
                System.out.println(String.format("[Market Matcher]: 当前订单 %s [%s] 和 对手订单 %s [%s] 没有一个为市价订单, 无法使用此撮合规则",
                                                 order.getId(), order.getType().name(),
                                                 opponentOrder.getId(), opponentOrder.getType().name()));
            }
            return false;
        }

        // 不允许自身撮合
//        if (order.getUid().equals(opponentOrder.getUid())) {
//            return false;
//        }

        MarketManager marketMgr = this.ctx().getMarketMgr();
        BigDecimal marketPrice = marketMgr.getMarketPrice(order);
        this.ctx().setAttribute(order.getId(), marketPrice);

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
        if (!TradeHelper.isHasEnoughAmount(order, opponentPrice)) {

            if (me.isEnableLog()) {
                System.out.println(String.format("[Market Matcher]: 订单没有足够的钱进行购买 当前订单 %s [%s] 剩余金额: %s 对手订单单价为: %s",
                                                 order.getId(), order.getSide().name(),
                                                 order.getLeavesAmount().toPlainString(),
                                                 opponentPrice.toPlainString()));
            }

            return false;
        } else if (!TradeHelper.isHasEnoughAmount(opponentOrder, price)) {
            if (me.isEnableLog()) {
                System.out.println(String.format("[Market Matcher]: 订单没有足够的钱进行购买 当前订单 %s [%s] 剩余金额: %s 对手订单单价为: %s",
                                                 opponentOrder.getId(), opponentOrder.getSide().name(),
                                                 opponentOrder.getLeavesAmount().toPlainString(),
                                                 price.toPlainString()));
            }
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

        if (!arbitrage) {

            if (me.isEnableLog()) {
                if (order.isBuy()) {
                    System.out.println(String.format("[Market Matcher]: 不满足撮合条件 当前订单 %s [%s][%s] 价格 %s " +
                                                             "必须大于对手订单 %s [%s][%s] 价格 %s ",
                                                     order.getId(), order.getSide().name(), order.getType().name(), price.toPlainString(),
                                                     opponentOrder.getId(), opponentOrder.getSide(), opponentOrder.getType().name(), opponentPrice.toPlainString()));
                } else {
                    System.out.println(String.format("[Market Matcher]: 不满足撮合条件 当前订单 %s [%s][%s] 价格 %s " +
                                                             "必须小于对手订单 %s [%s][%s] 价格 %s ",
                                                     order.getId(), order.getSide().name(), order.getType().name(), price.toPlainString(),
                                                     opponentOrder.getId(), opponentOrder.getSide().name(), opponentOrder.getType().name(), opponentPrice.toPlainString()));
                }
            }

            return false;
        }


        TradeResult ts = this.doTrade(order, opponentOrder);
        BigDecimal amount = ts.getQuantity()
                              .multiply(ts.getExecutePrice())
                              .setScale(8, BigDecimal.ROUND_DOWN);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return false;
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
        BigDecimal marketPrice = this.ctx().getAttribute(order.getId());
        if (marketPrice == null) {
            throw new IllegalStateException("请勿多线程撮合");
        }
        return TradeHelper.genericTrade(order, opponentOrder, marketPrice);
    }
}
