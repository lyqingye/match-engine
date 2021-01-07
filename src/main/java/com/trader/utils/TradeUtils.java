package com.trader.utils;

import com.trader.core.entity.Order;
import com.trader.core.matcher.MatchResult;

import java.math.BigDecimal;

import static com.trader.utils.ArithmeticUtils.div;
import static com.trader.utils.ArithmeticUtils.mul;

/**
 * @author yjt
 * @since 2020/9/20 上午9:34
 */
public final class TradeUtils {
    /**
     * 计算成交价
     *
     * @param order         订单
     * @param opponentOrder 对手订单
     * @param marketPrice   市场价格（可以为空）
     * @return 成交价格
     */
    public static MatchResult calcExecutePrice(Order order,
                                               Order opponentOrder,
                                               BigDecimal marketPrice) {

        BigDecimal price = order.isMarketOrder() ? marketPrice : order.getBoundPrice();
        BigDecimal opponentPrice = opponentOrder.isMarketOrder() ? marketPrice : opponentOrder.getBoundPrice();

        if (price == null || opponentPrice == null) {
            throw new IllegalArgumentException("计算成交价失败, 非法价格");
        }

        MatchResult ts = new MatchResult();

        switch (order.getDiffPriceStrategy()) {
            // 驱动方吃到差价
            case DRIVER: {
                ts.setExecutePrice(opponentPrice);
                ts.setOpponentExecutePrice(opponentPrice);
                break;
            }
            // 平台通吃
            case PLATFORM: {
                if (!order.isMarketOrder()) {
                    price = order.getBoundPrice();
                }
                if (!opponentOrder.isMarketOrder()) {
                    opponentPrice = opponentOrder.getBoundPrice();
                }
                ts.setExecutePrice(price);
                ts.setOpponentExecutePrice(opponentPrice);
                break;
            }

            // 最早挂单者吃到差价
            case TIME_FIRST: {
                if (order.getCreateDateTime().before(opponentOrder.getCreateDateTime())) {
                    ts.setExecutePrice(opponentPrice);
                    ts.setOpponentExecutePrice(opponentPrice);
                } else {
                    ts.setExecutePrice(price);
                    ts.setOpponentExecutePrice(price);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("非法差价策略");
            }
        }
        return ts;
    }


    /**
     * 通用的撮合, 所有的订单必须遵循以下规定:
     * 1. 买入单, 无论是限价还是市价还是止盈止损单, 都只用总金额进行计算
     * 2. 卖出单, 无论是限价还是市价还是止盈止损单, 都只用总数量进行计算
     * <p>
     * 满足以上条件, 就可以统一成一个撮合规则
     *
     * @param order         当前订单
     * @param opponentOrder 对手订单
     * @param marketPrice   市场价格
     * @return 撮合结果
     */
    public static MatchResult genericTrade(Order order,
                                           Order opponentOrder,
                                           BigDecimal marketPrice) {
        if (order.isBuy()) {
            return genericTradeInternal(order, opponentOrder, marketPrice);
        } else {
            return genericTradeInternal(opponentOrder, order, marketPrice);
        }
    }

    private static MatchResult genericTradeInternal(Order buyOrder,
                                                    Order sellOrder,
                                                    BigDecimal marketPrice) {
        //
        // 计算成交价
        //
        MatchResult ts = TradeUtils.calcExecutePrice(buyOrder,
                                                     sellOrder,
                                                     marketPrice);
        BigDecimal buyerExecPrice = ts.getExecutePrice();
        BigDecimal sellerExecPrice = ts.getOpponentExecutePrice();

        //
        // 计算最终成交量
        //
        BigDecimal sellQty = sellOrder.getLeavesQuantity();

        //
        // 买入单, 待执行金额 / 成交价 = 待执行数量
        //
        BigDecimal buyQty = div(buyOrder.getLeavesAmount(), buyerExecPrice);
        BigDecimal buyerExecAmount = mul(buyerExecPrice, sellQty);

        // 买家全部成交 | 卖家全部成交或成交部分
        if (buyQty.compareTo(sellQty) <= 0) {
            if (buyerExecAmount.compareTo(buyOrder.getLeavesAmount()) == 0) {
                // 买卖单全部买完
                ts.setQuantity(sellQty);
            } else {
                // 买单全部买完
                ts.setQuantity(buyQty);
            }
            ts.setExecuteAmount(buyOrder.getLeavesAmount());
            // 计算对手卖单收入的计价货币 = 对手订单成交单价 * 成交数量
            ts.setOpponentExecuteAmount(mul(ts.getQuantity(), sellerExecPrice));
        } else {
            // 买单不能全部买完, 但卖单可以卖完
            // 所以成交金额则是 卖单的数量 * 当前订单单价
            ts.setExecuteAmount(buyerExecAmount);
            ts.setQuantity(sellQty);

            // 计算对手卖单收入的计价货币 = 对手订单成交单价 * 成交数量
            ts.setOpponentExecuteAmount(mul(sellQty, sellerExecPrice));
        }
        ts.setTimestamp(System.currentTimeMillis());
        return ts;
    }

    /**
     * 判断买家的钱是否能够买得起最少的数量
     * <p>
     * 参考: {@link #genericTrade}
     *
     * @param order     订单
     * @param unitPrice 单价
     * @return 是否能够买得起最少的数量
     */
    public static boolean isHasEnoughAmount(Order order, BigDecimal unitPrice) {
        //
        // 如果买家剩余的钱连 0.00000001 都买不起那就直接无法撮合, 因为成交数量不能为空
        //
        if (order.isBuy()) {
            return div(order.getLeavesAmount(), unitPrice).compareTo(BigDecimal.ZERO) != 0;
        }
        return true;
    }

    /**
     * 判断一个订单是否已经结束
     * <p>
     * 参考: {@link #genericTrade}
     *
     * @param order 订单
     * @return 订单是否已经结束
     */
    public static boolean isFinished(Order order) {

        if (order.isFinished() || order.isCanceled()) {
            return true;
        }

        switch (order.getType()) {
            case MARKET:
            case STOP:
            case LIMIT: {
                if (order.isBuy()) {
                    return order.getLeavesAmount().compareTo(BigDecimal.ZERO) == 0;
                }
                return order.getLeavesQuantity().compareTo(BigDecimal.ZERO) == 0;
            }
            default: {
                throw new IllegalArgumentException("非法订单类型");
            }
        }
    }

}
