package com.trader.market.publish;

import com.trader.utils.ThreadPoolUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClientOptions;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author yjt
 * @since 2020/10/11 11:36
 */
public interface MarketPublishClient {
    /**
     * 链接到目标
     *
     * @param host 域名
     * @param port 端口
     * @param consumer 消息消费者
     */
    void conn(String host, int port, Consumer<Buffer> consumer) ;

    /**
     * 推送消息
     *
     * @param textMsg 文本消息
     */
     void send (String textMsg);

    /**
     * 是否已经连接上了
     */
    boolean isOpen ();

    /**
     * 推送消息
     *
     * @param binMsg 二进制消息
     */
    void send (byte[] binMsg);

    /**
     * 关闭链接
     */
    void close();

    /**
     * 域名
     * @return 域名
     */
    String host();

    /**
     * 端口
     *
     * @return 端口
     */
    int port ();

    /**
     * 消费者
     *
     * @return 消费者
     */
    Consumer<Buffer> consumer ();

    /**
     * 添加消费者
     */
    void setConsumer(Consumer<Buffer> consumer);
}
