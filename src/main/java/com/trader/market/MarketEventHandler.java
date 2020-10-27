package com.trader.market;

import com.trader.def.OrderSide;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.matcher.TradeResult;

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
     * @param msg
     */
    default void onMarketPriceChange(PriceChangeMessage msg) {
    }

    /**
     * 交易成功事件
     *
     * @param symbolId
     *         交易对
     * @param side
     *         买入 / 卖出
     * @param ts
     *         撮合结果
     */
    default void onTrade(String symbolId, OrderSide side, TradeResult ts) {
    }
}
