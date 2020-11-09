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

    /**
     * 获取盘口买一信息
     *
     * @return 盘口买一 or null
     */
    public MarketDepthInfo topBid() {
        if (series.isEmpty()) {
            return null;
        }
        List<MarketDepthInfo> bids = series.get(0).getBids();
        if (bids.isEmpty()) {
            return null;
        }
        return bids.get(0);
    }

    /**
     * 获取盘口卖一信息
     *
     * @return 盘口卖一 or null
     */
    public MarketDepthInfo topAsk() {
        if (series.isEmpty()) {
            return null;
        }
        List<MarketDepthInfo> asks = series.get(0).getAsks();
        if (asks.isEmpty()) {
            return null;
        }
        return asks.get(0);
    }
}
