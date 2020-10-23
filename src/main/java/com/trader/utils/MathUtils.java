package com.trader.utils;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/17 下午5:33
 */
public final class MathUtils {

    /**
     * 取最小值
     *
     * @param a
     * @param b
     *
     * @return 最小值
     */
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) > 0) {
            return b;
        }
        return a;
    }

    /**
     * 取最大值
     *
     * @param a
     * @param b
     *
     * @return 最大值
     */
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) > 0) {
            return a;
        }
        return b;
    }
}
