package com.trader.def;

import lombok.Getter;

/**
 * 订单成交方式
 *
 * @author yjt
 * @since 2020/9/1 上午9:27
 */
@Getter
public enum OrderTimeInForce {

    /**
     * 取消前一直有效 (Good till cancelled)
     */
    GTC,

    /**
     * 立即成交或取消 (Immediate closing or cancellation)
     */
    IOC,

    /**
     * 全部执行或取消 (Fill ok kill)
     */
    FOK,

    /**
     * 一次性全部执行或不执行 (All or none)
     */
    AON
}
