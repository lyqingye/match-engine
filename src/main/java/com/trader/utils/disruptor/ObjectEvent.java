package com.trader.utils.disruptor;

import lombok.Data;

/**
 * @author yjt
 * @since 2020/9/24 下午3:26
 */
@Data
public class ObjectEvent<T> {
    private T obj;

    public void clear () {
        this.obj = null;
    }
}
