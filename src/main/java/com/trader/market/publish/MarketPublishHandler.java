package com.trader.market.publish;

import com.trader.def.OrderSide;
import com.trader.market.MarketEventHandler;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.utils.GZIPUtils;
import io.vertx.core.json.Json;
import lombok.Getter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 消息推送
 *
 * @author yjt
 * @since 2020/10/10 下午5:45
 */
public class MarketPublishHandler implements MarketEventHandler {
    /**
     * 发布客户端
     */
    @Getter
    private MarketPublishClient client;

    public MarketPublishHandler (MarketPublishClient client) {
        this.client = Objects.requireNonNull(client);
        this.client.conn(client.host(),client.port(),client.consumer());
    }

    @Override
    public void onDepthChartChange(MarketDepthChartSeries series) {
        if(client.isOpen()) {
            client.send(Json.encode(series));
        }
        System.out.println();
    }

    /**
     * 市价价格变动事件
     *  @param symbol
     *         交易对
     * @param latestPrice
     *         最新成交价
     * @param third
     *         是否为第三方数据
     */
    @Override
    public void onMarketPriceChange(String symbol,
                                    BigDecimal latestPrice, boolean third) {
        if (!third) {
            // TODO
        }
    }

    /**
     * 交易成功事件
     *
     * @param symbolId
     *         交易对
     * @param side
     *         买入 / 卖出
     * @param quantity
     *         成交数量
     * @param price
     *         成交价格
     * @param ts
     */
    @Override
    public void onTrade(String symbolId,
                        OrderSide side,
                        BigDecimal quantity,
                        BigDecimal price,
                        long ts) {
        // TODO
    }
}
