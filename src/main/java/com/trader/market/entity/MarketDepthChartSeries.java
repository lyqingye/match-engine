package com.trader.market.entity;

import lombok.Data;

import java.util.List;

/**
 * @author yjt
 * @since 2020/10/11 12:45
 */
@Data
public class MarketDepthChartSeries {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 深度数据
     */
    private List<MarketDepthChart> series;
}
