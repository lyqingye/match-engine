package com.trader.utils.disruptor;

/**
 * @author yjt
 * @since 2020/9/24 下午3:26
 */
public class ObjectEvent<T> {

    private T obj;

    public ObjectEvent() {
    }

    public T getObj() {
        return this.obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
