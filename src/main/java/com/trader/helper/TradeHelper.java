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
     * 计算最终成交价
     *
     * @param type {@link ExecutePriceType}
     * @param price 当前订单价格
     * @param opponentPrice 对手盘价格
     * @return 最终成交价格
     */
    public static final BigDecimal calcExecutePrice (ExecutePriceType type,
                                                     BigDecimal price,
                                                     BigDecimal opponentPrice) {

        switch (type) {
            case SELF: {
                return price;
            }

            case MIDDLE: {
                return price.add(opponentPrice)
                            .divide(BigDecimal.valueOf(2),RoundingMode.DOWN);
            }

            case OPPONENT: {
                return opponentPrice;
            }

            case RANDOM: {
                return MathUtils.random(MathUtils.min(price,opponentPrice),
                                        MathUtils.max(price,opponentPrice));
            }

            default: {throw new IllegalArgumentException("不支持的成交价方式");}
        }
    }

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
