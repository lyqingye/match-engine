package com.trader.market.publish;

import com.trader.def.OrderSide;
import com.trader.market.MarketEventHandler;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.publish.msg.DepthChartMessage;
import com.trader.market.publish.msg.TradeMessage;
import com.trader.matcher.TradeResult;
import io.vertx.core.json.Json;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 消息推送
 *
 * @author yjt
 * @since 2020/10/10 下午5:45
 */
public class MarketPublishHandler implements MarketEventHandler {

    /**
     * 深度图缓存, 用于连接到服务后推送
     */
    private Map<String, String> latestChartCache = new HashMap<>(16);

    /**
     * 发布客户端
     */
    @Getter
    private MarketPublishClient client;

    public MarketPublishHandler (MarketPublishClient client) {
        this.client = Objects.requireNonNull(client);
        this.client.conn(client.host(),client.port(),client.consumer(),ar -> {
            if (ar.succeeded()) {
                latestChartCache.values().forEach(this.client::send);
            }
        });
    }

    public MarketPublishHandler (MarketPublishClient client, Consumer<MarketPublishHandler> onConnection) {
        this.client = Objects.requireNonNull(client);
        this.client.conn(client.host(),client.port(),client.consumer(),ar -> {
            if (ar.succeeded()) {
                onConnection.accept(this);
            }
        });
    }

    @Override
    public void onDepthChartChange(MarketDepthChartSeries series) {
        String obj = Json.encode(DepthChartMessage.of(series));
        latestChartCache.put(series.getSymbol(),obj);
        if(client.isOpen()) {
            client.send(obj);
        }
    }

    /**
     * 交易成功事件
     *
     * @param symbolId
     *         交易对
     * @param side
     *         买入 / 卖出
     * @param ts
     */
    @Override
    public void onTrade(String symbolId,
                        OrderSide side,
                        TradeResult ts) {
        final TradeMessage tradeResult = new TradeMessage();
        tradeResult.setSymbol(symbolId);
        tradeResult.setQuantity(ts.getQuantity());
        tradeResult.setPrice(ts.getExecutePrice());
        tradeResult.setTs(ts.getTimestamp());
        tradeResult.setDirection(side.toDirection());
        if (client.isOpen()) {
            client.send(Json.encode(TradeMessage.of(tradeResult)));
        }
    }
}
