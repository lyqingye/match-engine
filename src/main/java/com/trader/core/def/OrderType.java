package com.trader.core.def;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/9/1 上午9:02
 */
@AllArgsConstructor
@Getter
public enum OrderType {
    /**
     * 市价交易
     */
    MARKET(3),

    /**
     * 限价交易
     */
    LIMIT(2),

    /**
     * 止盈止损
     */
    STOP(1);

    /**
     * 优先级
     */
    private int priority;

}
