package com.trader.matcher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午2:18
 */
@Data
@AllArgsConstructor
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
     * 真实的成交价
     */
    private BigDecimal actualPrice;

    /**
     * 成交量
     */
    private BigDecimal quantity;
}
