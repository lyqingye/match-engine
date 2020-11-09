package com.trader.utils.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    public static <T> DisruptorQueue<T> createSingleQueue(int queueSize,
                                                          ThreadFactory threadFactory,
                                                          AbstractDisruptorConsumer<T> consumer) {
        Disruptor<ObjectEvent<T>> disruptor = new Disruptor<>(new ObjectEventFactory<T>(),
                queueSize, threadFactory,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());
        disruptor.handleEventsWith(consumer);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<ObjectEvent<T>>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, ObjectEvent<T> event) {
                System.out.println(String.format("[Disruptor]: uncaught handle event exception thread: %s event: %s",
                        Thread.currentThread().getName(), event.getObj().toString()));
                ex.printStackTrace();
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                System.out.println(String.format("[Disruptor]: uncaught on start exception thread: %s",
                        Thread.currentThread().getName()));
                ex.printStackTrace();
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                System.out.println(String.format("[Disruptor]: uncaught on shutdown exception thread: %s",
                        Thread.currentThread().getName()));
                ex.printStackTrace();
            }
        });
        return new DisruptorQueue<T>(disruptor);
    }

    public static <T> DisruptorQueue<T> createQueue(int queueSize,
                                                    ThreadFactory threadFactory,
                                                    AbstractDisruptorConsumer<T> consumer) {
        Disruptor<ObjectEvent<T>> disruptor = new Disruptor<>(new ObjectEventFactory<T>(),
                                                              queueSize, threadFactory,
                                                              ProducerType.MULTI,
                                                              new BlockingWaitStrategy());
        disruptor.handleEventsWith(consumer);
        return new DisruptorQueue<T>(disruptor);
    }
}
