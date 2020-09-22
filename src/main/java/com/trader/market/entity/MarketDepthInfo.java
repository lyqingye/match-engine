package com.trader.market.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/22 上午10:50
 */
@Data
public class MarketDepthInfo {

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private BigDecimal deal;

    /**
     * 剩余量
     */
    private BigDecimal leaves;

    /**
     * 总量
     */
    private BigDecimal total;
}
