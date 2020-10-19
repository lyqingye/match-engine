package com.trader.utils;

import com.trader.entity.Order;
import com.trader.helper.tuples.Tuple;

/**
 * @author yjt
 * @since 2020/9/24 上午8:56
 */

public final class SymbolUtils {

    /**
     * 生成交易对
     *
     * @param coinId
     *         币种Id
     * @param currencyId
     *         计价货币Id
     *
     * @return 交易对
     */
    public static String makeSymbol(String coinId, String currencyId) {
        return coinId.toUpperCase() + "-" + currencyId.toUpperCase();
    }

    /**
     * 生成交易对
     *
     * @param tuple
     *         元组
     *
     * @return 交易对
     */
    public static String makeSymbol(Tuple<String, String> tuple) {
        return makeSymbol(tuple.getO1(), tuple.getO2());
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
