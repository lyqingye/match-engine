package com.trader.def;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/9/1 上午9:17
 */
@AllArgsConstructor
@Getter
public enum UpdateType {
    /**
     * 空
     */
    NONE,

    /**
     * 添加
     */
    ADD,

    /**
     * 更新
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE
}
