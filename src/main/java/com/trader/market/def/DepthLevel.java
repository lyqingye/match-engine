package com.trader.market.def;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yjt
 * @since 2020/10/11 12:40
 */
@AllArgsConstructor
@Getter
public enum DepthLevel {
    step0,
    step1,
    step2,
    step3,
    step4,
    step5;

    public static DepthLevel of(byte ordinal) {
        for (DepthLevel lv : values()) {
            if (lv.ordinal() == ordinal) {
                return lv;
            }
        }
        return null;
    }
}
