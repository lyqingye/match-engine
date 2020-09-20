package com.trader.matcher;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午2:18
 */
@Data
public class TradeResult {
    /**
     * 最终的成交价
     */
    private BigDecimal executePrice;

    /**
     * 成交量
     */
    private BigDecimal quantity;
}
