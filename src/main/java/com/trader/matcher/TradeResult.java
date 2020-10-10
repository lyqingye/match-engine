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
     * 对手订单最终的成交价 (市价订单没有对手订单最终成交价)
     */
    private BigDecimal opponentExecutePrice = BigDecimal.ZERO;

    /**
     * 成交量
     */
    private BigDecimal quantity;

    /**
     * 交易时间
     */
    private long timestamp;
}
