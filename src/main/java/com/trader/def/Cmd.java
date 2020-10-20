package com.trader.def;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/10/20 上午12:28
 */
@AllArgsConstructor
@Getter
public enum Cmd {
    ADD_ORDER,
    CANCEL_ORDER
}
