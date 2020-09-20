package com.trader.def;

/**
 * 成交价取值方式
 *
 * @author yjt
 * @since 2020/9/20 上午9:16
 */
public enum ExecutePriceType {

    /**
     * 最终成交价格为对手盘价格
     */
    OPPONENT,

    /**
     * 最终成交价为自身挂单价格
     */
    SELF,

    /**
     * 最终成交价为中间价
     */
    MIDDLE,

    /**
     * 最终成交价为价格之间的随机值
     */
    RANDOM
}
