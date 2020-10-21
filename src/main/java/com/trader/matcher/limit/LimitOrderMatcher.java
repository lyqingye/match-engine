package com.trader.matcher.limit;

import com.trader.MatchEngine;
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
        MatchEngine me = ctx().getEngine();

        //
        // 处理以下类型的订单
        //
        // STOP <-> LIMIT   （止盈止损单和限价单）
        // LIMIT <-> LIMIT  （限价单和限价单）
        //

        if (order.isMarketOrder() || opponentOrder.isMarketOrder()) {
            if (me.isEnableLog()) {
                System.out.println(String.format("[Limit Matcher]: 当前订单 %s [%s] 和 对手订单 %s [%s] 其中一个为市价订单, 无法使用此撮合规则",
                                                 order.getId(), order.getType().name(),
                                                 opponentOrder.getId(), opponentOrder.getType().name()));
            }
            return false;
        }

        // 不允许自身撮合
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

        //
        // 判断是否有足够的钱进行购买
        //
        if (!TradeHelper.isHasEnoughAmount(order, opponentPrice)) {

            if (me.isEnableLog()) {
                System.out.println(String.format("[Limit Matcher]: 订单没有足够的钱进行购买 当前订单 %s [%s] 剩余金额: %s 对手订单单价为: %s",
                                                 order.getId(), order.getSide().name(),
                                                 order.getLeavesAmount().toPlainString(),
                                                 opponentPrice.toPlainString()));
            }

            return false;
        } else if (!TradeHelper.isHasEnoughAmount(opponentOrder, price)) {
            if (me.isEnableLog()) {
                System.out.println(String.format("[Limit Matcher]: 订单没有足够的钱进行购买 当前订单 %s [%s] 剩余金额: %s 对手订单单价为: %s",
                                                 opponentOrder.getId(), opponentOrder.getSide().name(),
                                                 opponentOrder.getLeavesAmount().toPlainString(),
                                                 price.toPlainString()));
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
