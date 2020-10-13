package com.trader.market.publish.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/10/11 11:49
 */
@AllArgsConstructor
@Getter
public enum MessageType {
    MARKET_PRICE,
    DEPTH_CHART,
    TRADE_RESULT;

    public static MessageType ofName (String name) {
        for (MessageType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }
}
