package com.trader.def;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/10/25 16:43
 */
@AllArgsConstructor
@Getter
public enum Category {
    /**
     * 用户下的单
     */
    USER,

    /**
     * 普通机器人下的单
     */
    GENERIC_BOT,

    /**
     * 控盘机器人
     */
    TRADING_BOT,

    /**
     * 单纯影响K线的机器人
     */
    KLINE_BOT
}
