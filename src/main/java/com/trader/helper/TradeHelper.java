package com.trader.helper;

import com.trader.def.ExecutePriceType;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;
import com.trader.utils.MathUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author yjt
 * @since 2020/9/20 上午9:34
 */
public class TradeHelper {

    /**
     * 判断买家的钱是否能够买得起最少的数量
     *
     * @param order 订单
     * @param unitPrice 单价
     * @return 是否能够买得起最少的数量
     */
    public static boolean isHasEnoughAmount(Order order, BigDecimal unitPrice) {
        //
        // 如果买家剩余的钱连 0.00000001 都买不起那就直接无法撮合, 因为成交数量不能为空
        //
        if (order.isBuy()) {
            if (order.getLeavesAmount().divide(unitPrice, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断一个订单是否已经结束
     *
     * @param order 订单
     * @return
     */
    public static boolean isFinished (Order order) {
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

    /**
     * 计算成交价
     *
     * @param order 订单
     * @param opponentOrder 对手订单
     * @param marketPrice 市场价格（可以为空）
     * @return 成交价格
     */
    public static TradeResult calcExecutePrice (Order order,
                                                Order opponentOrder,
                                                BigDecimal marketPrice) {

        BigDecimal price = order.isMarketOrder() ? marketPrice :order.getBoundPrice();
        BigDecimal opponentPrice = opponentOrder.isMarketOrder() ? marketPrice :opponentOrder.getBoundPrice();

        if (price == null || opponentPrice == null) {
            throw new IllegalArgumentException("计算成交价失败, 非法价格");
        }

        TradeResult ts = new TradeResult();
        switch (order.getDifferencePriceStrategy()) {
            case PLATFORM: {
                // 平台通吃
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

            case TIME_FIRST: {

                // 最早挂单者吃到差价
                if (order.getCreateDateTime().before(opponentOrder.getCreateDateTime())) {
                    ts.setExecutePrice(opponentPrice);
                    ts.setOpponentExecutePrice(opponentPrice);
                }else {
                    ts.setExecutePrice(price);
                    ts.setOpponentExecutePrice(price);
                }
                break;
            }

            case BUYER_FIRST: {

                // 买家吃到差价
                if (order.isBuy()) {
                    ts.setExecutePrice(opponentPrice);
                    ts.setOpponentExecutePrice(opponentPrice);
                }else {
                    ts.setExecutePrice(price);
                    ts.setOpponentExecutePrice(price);
                }
                break;
            }

            case SELLER_FIRST: {

                // 卖家吃到差价
                if (order.isSell()) {
                    ts.setExecutePrice(opponentPrice);
                    ts.setOpponentExecutePrice(opponentPrice);
                }else {
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
}
