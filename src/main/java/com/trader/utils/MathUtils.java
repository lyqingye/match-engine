package com.trader.utils;

import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    /**
     * 区间取随机值
     *
     * @param startInclusive
     *         >=
     * @param endInclusive
     *         <=
     *
     * @return 随机值
     */
    public static BigDecimal random(BigDecimal startInclusive,
                                    BigDecimal endInclusive) {
        double randomDouble = RandomUtils.nextDouble(startInclusive.doubleValue(),
                                                     endInclusive.doubleValue());
        return BigDecimal.valueOf(randomDouble)
                         .setScale(8, RoundingMode.DOWN);
    }
}
