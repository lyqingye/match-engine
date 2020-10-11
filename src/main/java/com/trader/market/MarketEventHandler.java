package com.trader.market;

import com.trader.def.OrderSide;
import com.trader.entity.OrderBook;
import com.trader.market.entity.MarketDepthChartSeries;

import java.math.BigDecimal;

/**
 * 市场事件处理器
 *
 * @author yjt
 * @since 2020/9/20 下午2:36
 */
public interface MarketEventHandler {

    /**
     * 盘口变动
     *
     * @param series
     *         盘口
     */
    default void onDepthChartChange(MarketDepthChartSeries series) {

    }

    /**
     * 市价价格变动事件
     *
     * @param symbol
     *         交易对
     * @param latestPrice
     *         最新价格
     * @param third
     *         是否为第三方数据
     */
    default void onMarketPriceChange(String symbol, BigDecimal latestPrice, boolean third) {
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
     *         成交时间
     */
    default void onTrade(String symbolId, OrderSide side, BigDecimal quantity, BigDecimal price, long ts) {
    }
}
