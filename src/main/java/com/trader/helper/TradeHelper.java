package com.trader.helper;

import com.trader.def.ExecutePriceType;
import com.trader.entity.Order;
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
}
