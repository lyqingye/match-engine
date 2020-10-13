package com.trader.market.publish;

import com.trader.utils.ThreadPoolUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
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
    private Consumer<JsonObject> consumer;

    /**
     * vertx 实例
     */
    private static final Vertx vertx;

    /**
     * socket
     */
    private NetSocket client;



    static {
        final VertxOptions options = new VertxOptions();
        options.setWorkerPoolSize(1)
               .setEventLoopPoolSize(1);
        vertx = Vertx.vertx(options);

    }

    public TcpMarketPublishClient (String host,int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 链接到目标
     *
     * @param host 域名
     * @param port 端口
     * @param consumer 消息消费者
     */
    @Override
    public void conn(String host, int port, Consumer<JsonObject> consumer, Handler<AsyncResult<NetSocket>> connectHandler)  {
        this.host = host;
        this.port = port;
        this.consumer = consumer;

        //
        // 解决黏包
        //
        RecordParser parser = RecordParser.newDelimited("\n", h -> {
            JsonObject json = null;
            try {
                json = h.toJsonObject();
            }catch (Exception e) {
                e.printStackTrace();
            }
            if (json == null) {
                return;
            }
            if (consumer != null) {
                consumer.accept(json);
            }
        });

        ThreadPoolUtils.submit(() -> {

            final NetClientOptions options = new NetClientOptions();

            // 设置重试次数和重试间隔时间
            options.setReconnectInterval(1000)
                   .setReconnectAttempts(Integer.MAX_VALUE)
                   .setLogActivity(true)
                   // 保持长连接
                   .setTcpKeepAlive(true);
            // 开始创建 socket 链接
            vertx.createNetClient(options)
                 .connect(port, host, ar -> {
                     if (ar.succeeded()) {
                         // 连接成功
                         client = ar.result();

                         System.out.println("success connection");

                         client.handler(parser::handle);

                         // 如果目标关闭则进行重连
                         client.closeHandler(close -> {
                             this.client = null;
                            conn(host,port,consumer,connectHandler);
                         });
                     }
                 });
        });
    }

    /**
     * 推送消息
     *
     * @param textMsg 文本消息
     */
    @Override
    public void send (String textMsg) {
        if (this.client != null) {
            this.client.write(textMsg + "\n", StandardCharsets.UTF_8.name());
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
     * @param binMsg 二进制消息
     */
    @Override
    public void send (byte[] binMsg) {
        if (this.client != null) {
            this.client.write(Buffer.buffer(binMsg));
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
    public Consumer<JsonObject> consumer() {
        return this.consumer;
    }
}
