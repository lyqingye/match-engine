package com.trader.utils;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/17 下午5:33
 */
public final class MathUtils {

    public static BigDecimal min (BigDecimal a,BigDecimal b) {
        if (a.compareTo(b) > 0) {
            return b;
        }
        return a;
    }

    public static BigDecimal max (BigDecimal a,BigDecimal b) {
        if (a.compareTo(b) > 0) {
            return a;
        }
        return b;
    }
}
