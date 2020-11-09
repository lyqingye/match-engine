package com.trader.market.entity;

import com.trader.market.def.DepthLevel;
import lombok.Data;

import java.util.List;

/**
 * @author yjt
 * @since 2020/9/22 上午10:55
 */
@Data
public class MarketDepthChart {

    /**
     * 深度
     */
    private DepthLevel depth;

    /**
     * 买盘
     */
    private List<MarketDepthInfo> bids;

    /**
     * 卖盘
     */
    private List<MarketDepthInfo> asks;
}
