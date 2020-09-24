package com.trader.utils.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * @author yjt
 * @since 2020/9/24 下午3:49
 */
public class DisruptorQueueFactory {

    public static <T> DisruptorQueue<T> createQueue(int queueSize,
                                                    AbstractDisruptorConsumer<T> consumer) {
        Disruptor<ObjectEvent<T>> disruptor = new Disruptor<>(new ObjectEventFactory<T>(),
                                                              queueSize, Executors.defaultThreadFactory(),
                                                              ProducerType.MULTI,
                                                              new BlockingWaitStrategy());
        disruptor.handleEventsWith(consumer);
        return new DisruptorQueue<T>(disruptor);
    }
}