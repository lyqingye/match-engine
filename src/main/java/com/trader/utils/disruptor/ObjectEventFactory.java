package com.trader.utils.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author yjt
 * @since 2020/9/24 下午3:27
 */
public class ObjectEventFactory<T> implements EventFactory<ObjectEvent<T>> {
    public ObjectEventFactory() { }

    @Override
    public ObjectEvent<T> newInstance() {
        return new ObjectEvent();
    }
}
