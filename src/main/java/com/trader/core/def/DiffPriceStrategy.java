package com.trader.core.def;


/**
 * 差价取值策略
 *
 * @author yjt
 * @since 2020/9/21 下午1:32
 */
public enum DiffPriceStrategy {
    /**
     * 驱动订单
     */
    DRIVER,

    /**
     * 先挂单者优先
     */
    TIME_FIRST,

    /**
     * 平台通吃
     */
    PLATFORM;
}
