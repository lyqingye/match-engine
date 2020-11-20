package com.trader.market.publish;

import com.trader.market.publish.config.MarketConfigHttpClient;
import com.trader.market.publish.msg.Message;
import com.trader.utils.ThreadPoolUtils;
import com.trader.utils.VertxUtils;
import com.trader.utils.messages.FrameParser;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author yjt
 * @since 2020/10/10 下午6:51
 */
public class TcpMarketPublishClient implements MarketPublishClient {
    /**
     * 域名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 消费者
     */
    @Setter
    private Consumer<Message<?>> consumer;

    /**
     * socket
     */
    private NetSocket client;

    /**
     * 是否正在运行
     */
    private volatile boolean isRunning;

    /**
     * 解析器
     */
    private FrameParser parser;

    /**
     * 创建市场管理器配置客户端
     *
     * @param host
     *         host
     * @param port
     *         port
     *
     * @return client
     */
    public static MarketConfigHttpClient createConfigClient(String host, int port) {
        return new MarketConfigHttpClient(VertxUtils.vertx(), host, port);
    }

    public TcpMarketPublishClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.parser = new FrameParser(ar -> {
            if (ar.succeeded()) {
                if (this.consumer != null) {
                    this.consumer.accept(ar.result());
                }
            }
        });
    }

    /**
     * 链接到目标
     *
     * @param host
     *         域名
     * @param port
     *         端口
     * @param consumer
     *         消息消费者
     */
    @Override
    public void conn(String host, int port, Consumer<Message<?>> consumer, Handler<AsyncResult<NetSocket>> connectHandler) {
        this.host = host;
        this.port = port;
        this.consumer = consumer;

        ThreadPoolUtils.submit(() -> {

            final NetClientOptions options = new NetClientOptions();

            // 设置重试次数和重试间隔时间
            options.setReconnectInterval(1000)
                   .setReconnectAttempts(Integer.MAX_VALUE)
                   // 保持长连接
                   .setTcpKeepAlive(true);
            // 开始创建 socket 链接
            VertxUtils.vertx().createNetClient(options)
                 .connect(port, host, ar -> {
                     if (ar.succeeded()) {
                         // 连接成功
                         client = ar.result();
                         client.setWriteQueueMaxSize(4096);

                         if (connectHandler != null) {
                             connectHandler.handle(ar);
                         }

                         System.out.println("[MarketPublish]: success connection to the server");

                         client.handler(parser);
                         this.isRunning = true;

                         // 如果目标关闭则进行重连
                         client.closeHandler(close -> {
                             this.client = null;
                             if (this.isRunning) {
                                 conn(host, port, consumer, connectHandler);
                             }
                         });
                     }
                 });
        });
    }

    /**
     * 推送消息
     *
     * @param textMsg
     *         文本消息
     */
    @Override
    public void send(String textMsg) {
        if (this.client != null) {
            // 换行符是用于解决拆包黏包, 这里省事, 如果发送的消息是文本, 那就就以换行符当作包的终止符
            this.client.write(textMsg + "\n", StandardCharsets.UTF_8.name());
            // 解决背压问题, 关键字 `back pressure`, 想了解的话就去了解下吧
            // 这边是选择丢弃消息
            if (this.client.writeQueueFull()) {
                this.client.pause();
                this.client.drainHandler(done -> this.client.resume());
            }
        }
    }

    @Override
    public boolean isOpen() {
        return this.client != null;
    }

    /**
     * 推送消息
     *
     * @param binMsg
     *         二进制消息
     */
    @Override
    public void send(byte[] binMsg) {
        if (this.client != null) {
            this.client.write(Buffer.buffer(binMsg));
            if (this.client.writeQueueFull()) {
                this.client.pause();
                this.client.drainHandler(done -> this.client.resume());
            }
        }
    }

    /**
     * 推送消息
     *
     * @param bufferMsg buffer
     */
    @Override
    public void send(Buffer bufferMsg) {
        if (this.client != null) {
            this.client.write(bufferMsg);
            if (this.client.writeQueueFull()) {
                this.client.pause();
                this.client.drainHandler(done -> this.client.resume());
            }
        }
    }

    /**
     * 关闭链接
     */
    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
            this.isRunning = false;
        }
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public Consumer<Message<?>> consumer() {
        return this.consumer;
    }
}
