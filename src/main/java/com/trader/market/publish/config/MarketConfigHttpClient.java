package com.trader.market.publish.config;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author yjt
 * @since 2020/10/15 下午1:50
 */
public class MarketConfigHttpClient {

    /**
     * vertx
     */
    private Vertx vertx;

    /**
     * 地址
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * client
     */
    private HttpClient client;


    public MarketConfigHttpClient(Vertx vertx, String host, int port) {
        this.vertx = vertx;
        this.host = host;
        this.port = port;

        HttpClientOptions options = new HttpClientOptions();
        options.setLogActivity(true);
        options.setConnectTimeout(5000);
        options.setDefaultHost(host);
        options.setDefaultPort(port);
        client = vertx.createHttpClient(options);
    }

    /**
     * 获取所有市场价格
     *
     * @return 市场价格
     */
    @SuppressWarnings({"unchecked", "deprecated"})
    public void getMarketPriceAsync(Handler<AsyncResult<Map<String, String>>> handler) {
        client.getNow("/market/price/latest", response -> {
            response.bodyHandler(body -> {
                handler.handle(Future.succeededFuture(body.toJsonObject().mapTo(Map.class)));
            });
        });
    }

    /**
     * 获取所有市场价格
     *
     * @return 市场价格
     */

    @SuppressWarnings({"unchecked", "deprecated"})
    public Map<String, String> getMarketPriceSync() {
        AtomicReference<CountDownLatch> monitor = new AtomicReference<>(new CountDownLatch(1));
        AtomicReference<Map<String, String>> result = new AtomicReference<>(Collections.emptyMap());
        client.getNow("/market/price/latest/v2", response -> {
            response.bodyHandler(body -> {
                result.set(body.toJsonObject().mapTo(Map.class));
                monitor.get().countDown();
            });
        });
        try {
            monitor.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.get();
    }
}