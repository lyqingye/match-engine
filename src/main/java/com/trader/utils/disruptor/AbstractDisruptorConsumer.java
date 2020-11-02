package com.trader.utils.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * Disruptor 抽象消费者
 *
 * @author yjt
 * @since 2020/9/24 下午3:29
 */
public abstract class AbstractDisruptorConsumer<T> implements EventHandler<ObjectEvent<T>>, WorkHandler<ObjectEvent<T>> {
    @Override
    public void onEvent(ObjectEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
        this.onEvent(event);
    }

    @Override
    public void onEvent(ObjectEvent<T> event) throws Exception {
        this.process(event.getObj());
        event.clear();
    }

    /**
     * 进行数据处理
     *
     * @param event
     *         事件
     */
    public abstract void process(T event);
}
