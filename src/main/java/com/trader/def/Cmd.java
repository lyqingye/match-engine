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
    /**
     * 添加订单的命令
     */
    ADD_ORDER,

    /**
     * 取消订单命令
     */
    CANCEL_ORDER,

    /**
     * 激活订单命令
     */
    ACTIVE_ORDER
}
