package com.trader.matcher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午2:18
 */
@Data
public class TradeResult {

    /**
     * 当前订单的成交价 (透明)
     */
    private BigDecimal price;

    /**
     * 对手订单的成交价 (透明)
     */
    private BigDecimal opponentPrice;

    /**
     * 最终的成交价
     */
    private BigDecimal executePrice;

    /**
     * 成交量
     */
    private BigDecimal quantity;
}
