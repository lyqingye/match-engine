package com.trader.market.entity;

import lombok.Data;

import java.util.Collection;

/**
 * @author yjt
 * @since 2020/9/22 上午10:55
 */
@Data
public class MarketDepthChart {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 深度
     */
    private Integer depth;

    /**
     * 买盘
     */
    private Collection<MarketDepthInfo> bids;

    /**
     * 卖盘
     */
    private Collection<MarketDepthInfo> asks;
}
