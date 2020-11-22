package com.trader.core.event;

import com.trader.core.def.EventType;

/**
 * 事件
 *
 * 标准：https://github.com/cloudevents/spec/blob/v1.0/spec.md
 */
public interface Event {

    /**
     * 事件ID
     *
     * @return 事件ID
     */
    long id();

    /**
     * 事件发生时间
     *
     * @return 事件发生时间
     */
    long time ();

    /**
     * 事件类型
     *
     * @return 事件类型
     */
    EventType type();

    /**
     * 版本
     * @return 版本
     */
    byte version();

    /**
     * 事件来源
     *
     * @return 事件来源
     */
    String source();

    /**
     * 事件携带的数据
     *
     * @return 事件携带的数据
     */
    Object data ();

    /**
     * 事件上下文
     *
     * @return 事件上下文
     */
    Object context ();
}
