package com.trader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定一个商品的最基本的信息,对被交易物品的抽象
 * 注： 只定义商品的描述, 而不包含商品的价值
 * 因为对于虚拟货币来说, 货币的价值跟货币本身的市场需求和供应有关 即：（非明码标价）
 *
 * 如果是一件商品, 那么可能他有固定的价值 (生产劳动时间)
 *
 * @author yjt
 * @since 2020/9/1 上午9:10
 */
@Getter
@AllArgsConstructor
public class Product {

    /**
     * 商品ID
     */
    private String id;

    /**
     * 商品名字
     */
    private String name;
}
