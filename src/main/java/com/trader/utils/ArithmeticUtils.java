package com.trader.utils;

import jdk.internal.vm.annotation.ForceInline;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算工具类
 */
public final class ArithmeticUtils {

    /**
     * 计算精度
     */
    public static Integer SCALE = 8;

    /**
     * 进位规则, 默认全部舍弃
     */
    public static RoundingMode MODE = RoundingMode.DOWN;

    @ForceInline
    public static BigDecimal div (BigDecimal a,BigDecimal b) {
        return a.divide(b, SCALE, MODE);
    }

    @ForceInline
    public static BigDecimal mul (BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(SCALE,MODE);
    }

    @ForceInline
    public static BigDecimal add (BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    @ForceInline
    public static BigDecimal sub(BigDecimal a,BigDecimal b) {
        return a.subtract(b);
    }

    @ForceInline
    public static BigDecimal round (BigDecimal val) {
        return val.setScale(SCALE,MODE);
    }
}
