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
     * 机器人下的单
     */
    BOT
}
