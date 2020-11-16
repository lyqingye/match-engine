package com.trader.core.exception;

import com.trader.utils.disruptor.ObjectEvent;

/**
 * @author yjt
 * @since 2020/10/23 上午9:32
 */
public interface MatchExceptionHandler {

    /**
     * 异常处理器
     *
     * @param threadName  出问题的线程
     * @param component   出问题的组件
     * @param throwable   异常信息
     * @param information 额外信息 (可能为空)
     */
    void handler(String threadName, String component, Throwable throwable, String information);

    /**
     * 转换为disruptor异常处理器
     *
     * @param <T> T
     * @return disruptor异常处理器
     */
    default <T> com.lmax.disruptor.ExceptionHandler<ObjectEvent<T>> toDisruptorHandler() {
        return new com.lmax.disruptor.ExceptionHandler<ObjectEvent<T>>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, ObjectEvent<T> event) {
                handler(Thread.currentThread().getName(), "Disruptor:handlerEvent", ex, event.getObj().toString());
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                handler(Thread.currentThread().getName(), "Disruptor:onStart", ex, null);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                handler(Thread.currentThread().getName(), "Disruptor:onShutdown", ex, null);
            }
        };
    }

    static MatchExceptionHandler defaultHandler() {
        return new MatchExceptionHandler() {
            /**
             * 异常处理器
             *
             * @param threadName  出问题的线程
             * @param component   出问题的组件
             * @param throwable   异常信息
             * @param information 额外信息 (可能为空)
             */
            @Override
            public void handler(String threadName, String component, Throwable throwable, String information) {
                System.err.println(String.format("[%s][%s]\n%s", threadName, component, information));
                throwable.printStackTrace();
            }
        };
    }
}
