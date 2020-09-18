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
     * 成交价
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private BigDecimal quantity;
}
