package com.trader.utils;

import com.trader.entity.Order;

/**
 * @author yjt
 * @since 2020/9/24 上午8:56
 */

public final class SymbolUtils {

    /**
     * 生成交易对
     *
     * @param productId
     *         产品Id
     * @param currencyId
     *         货币Id
     *
     * @return 交易对
     */
    public static String makeSymbol(String productId, String currencyId) {
        return productId.toUpperCase() + "-" + currencyId.toUpperCase();
    }


    /**
     * 生成交易对
     *
     * @param order
     *         订单
     *
     * @return 交易对
     */
    public static String makeSymbol(Order order) {
        return order.getSymbol();
    }
}
