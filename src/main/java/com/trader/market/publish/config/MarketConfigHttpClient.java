package com.trader.market.publish.config;

import com.trader.market.publish.config.dto.CollectorStatusDto;
import com.trader.utils.SymbolUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author yjt
 * @since 2020/10/15 下午1:50
 */
public class MarketConfigHttpClient {

    /**
     * vertx
     */
    protected Vertx vertx;

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
     * 更新市场价格
     *
     * @param symbol
     *         交易对
     * @param price
     *         价格
     */
    @SneakyThrows
    public void updateMarketPriceSync(String symbol, BigDecimal price) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("symbol", symbol);
        data.put("price", price.toPlainString());
        String encode = data.encode();
        client.put("/market/price", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 获取所有市场价格
     *
     * @return 市场价格
     */

    @SneakyThrows
    @SuppressWarnings({"unchecked", "deprecated"})
    public Map<String, String> getMarketPriceSync() {
        CompletableFuture<Map<String, String>> cf = new CompletableFuture<>();
        client.getNow("/market/price/latest/v2", response -> {
            response.bodyHandler(body -> {
                cf.complete(body.toJsonObject().mapTo(Map.class));
            });
        });
        return cf.get();
    }

    /**
     * 添加/修改交易对映射
     *
     * @param source 来源交易对
     */
    @SneakyThrows
    public void putSymbolMappingSync(String source) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("source", source);
        data.put("target", SymbolUtils.toGenericSymbol(source));
        String encode = data.encode();
        client.put("/market/symbol/c2g/mapping", response -> {
            response.bodyHandler(body -> {
              cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
                .write(encode).end();
        cf.get();
    }


    //
    // 2.0 API
    //

    /**
     * 获取所有收集器
     *
     * @return 收集器列表
     */
    @SneakyThrows
    public List<CollectorStatusDto> listCollector() {
        CompletableFuture<List<CollectorStatusDto>> cf = new CompletableFuture<>();
        client.getNow("/market/collector/list", response -> {
            response.bodyHandler(body -> {
                cf.complete(body.toJsonObject().getJsonArray("data")
                    .stream()
                    .map(obj -> {
                        JsonObject jsonObject = (JsonObject) obj;
                        return jsonObject.mapTo(CollectorStatusDto.class);
                    })
                    .collect(Collectors.toList()));
            });
        });
        return cf.get();
    }

    /**
     * 部署一个收集器
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void deployCollector(String collectorName) {
        Objects.requireNonNull(collectorName);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", collectorName);
        String encode = data.encode();
        client.put("/market/collector/deploy", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 取消部署一个收集器
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void undeployCollector(String collectorName) {
        Objects.requireNonNull(collectorName);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", collectorName);
        String encode = data.encode();
        client.delete("/market/collector/undeploy", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 启动一个收集器
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void startCollector(String collectorName) {
        Objects.requireNonNull(collectorName);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", collectorName);
        String encode = data.encode();
        client.put("/market/collector/start", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 停止一个收集器
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void stopCollector(String collectorName) {
        Objects.requireNonNull(collectorName);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", collectorName);
        String encode = data.encode();
        client.put("/market/collector/stop", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 收集器订阅交易对
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void collectorSub(String collectorName,String subSymbol) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", Objects.requireNonNull(collectorName));
        data.put("symbol", Objects.requireNonNull(subSymbol));
        String encode = data.encode();
        client.put("/market/collector/subscribe", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }

    /**
     * 收集器取消订阅交易对
     *
     * @param collectorName 收集器名称
     */
    @SneakyThrows
    public void collectorUnSub(String collectorName,String unSubSymbol) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        JsonObject data = new JsonObject();
        data.put("collectorName", Objects.requireNonNull(collectorName));
        data.put("symbol", Objects.requireNonNull(unSubSymbol));
        String encode = data.encode();
        client.put("/market/collector/unsubscribe", response -> {
            response.bodyHandler(body -> {
                cf.complete(null);
            });
        }).putHeader("Content-Length", String.valueOf(encode.length()))
              .write(encode).end();
        cf.get();
    }


    /**
     * 关闭连接
     */
    public void close() {
        this.client.close();
    }
}
