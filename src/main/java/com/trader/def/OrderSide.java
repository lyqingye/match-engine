package com.trader.def;

import lombok.Getter;

/**
 * @author yjt
 * @since 2020/9/1 上午9:24
 */
@Getter
public enum OrderSide {
    /**
     * 买入单
     */
    BUY,

    /**
     * 卖出单
     */
    SELL;

    public String toDirection() {
        return this.name().toLowerCase();
    }
}
