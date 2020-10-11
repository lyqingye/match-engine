package com.trader.market.publish.msg;

import lombok.Data;

/**
 * @author yjt
 * @since 2020/10/11 12:34
 */
@Data
public class Message<T> {
    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 发送时间
     */
    private final Long ts = System.currentTimeMillis();

    /**
     * 消息内容
     */
    private T data;
}
