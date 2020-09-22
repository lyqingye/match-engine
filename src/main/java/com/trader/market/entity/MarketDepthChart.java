package com.trader.market.entity;

import lombok.Data;

import java.util.List;

/**
 * @author yjt
 * @since 2020/9/22 上午10:55
 */
@Data
public class MarketDepthChart {

    /**
     * 买盘
     */
    private List<MarketDepthInfo> bids;

    /**
     * 卖盘
     */
    private List<MarketDepthInfo> asks;
}
