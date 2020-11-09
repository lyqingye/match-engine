package com.trader.core.def;

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

    public static OrderSide toSide(String name) {
        for (OrderSide side : values()) {
            if (side.name().equalsIgnoreCase(name)) {
                return side;
            }
        }
        return null;
    }

    public static OrderSide toSide(byte ordinal) {
        for (OrderSide side : values()) {
            if (side.ordinal() == ordinal) {
                return side;
            }
        }
        return null;
    }
}
