package com.trader.cmd;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 命令类型定义
 */
@AllArgsConstructor
@Getter
public enum CmdType {
    /**
     * 订单添加
     */
    ORDER_ADD,

    /**
     * 订单取消
     */
    ORDER_CANCEL,

    /**
     * 止盈止损单激活
     */
    ORDER_ACTIVE,

    /**
     * 市场价格变动
     */
    PRICE_CHANGE,
}
