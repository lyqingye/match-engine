package com.trader.market.publish.config;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;

import java.util.Map;

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
        client = vertx.createHttpClient(options);
    }

    public Map<String, String> getThirdToGenericMappings() {
        String query = client.request(HttpMethod.GET, "/market/price/latest")
                             .query();
        return null;
    }


}
