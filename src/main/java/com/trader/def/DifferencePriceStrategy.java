package com.trader.def;


/**
 * 差价取值策略
 *
 * @author yjt
 * @since 2020/9/21 下午1:32
 */
public enum DifferencePriceStrategy {

    /**
     * 买家先手
     */
    BUYER_FIRST,

    /**
     * 卖家先手
     */
    SELLER_FIRST,

    /**
     * 先挂单者优先
     */
    TIME_FIRST,

    /**
     * 平台通吃
     */
    PLATFORM;
}
