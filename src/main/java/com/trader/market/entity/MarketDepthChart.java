package com.trader.market.entity;

import com.trader.market.def.DepthLevel;
import lombok.Data;

import java.util.Collection;

/**
 * @author yjt
 * @since 2020/9/22 上午10:55
 */
@Data
public class MarketDepthChart {

    /**
     * 深度
     */
    private Byte depth;

    /**
     * 买盘
     */
    private Collection<MarketDepthInfo> bids;

    /**
     * 卖盘
     */
    private Collection<MarketDepthInfo> asks;
}
