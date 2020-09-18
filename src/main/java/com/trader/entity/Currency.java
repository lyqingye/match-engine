package com.trader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/9/18 下午4:42
 */
@Getter
@AllArgsConstructor
public class Currency {
    /**
     * 货币ID
     */
    private String id;

    /**
     * 货币名字
     */
    private String name;
}
