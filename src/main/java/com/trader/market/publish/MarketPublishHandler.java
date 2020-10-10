package com.trader.market.publish;

import com.trader.def.OrderSide;
import com.trader.market.MarketEventHandler;

import java.math.BigDecimal;

/**
 * 消息推送
 *
 * @author yjt
 * @since 2020/10/10 下午5:45
 */
public class MarketPublishHandler implements MarketEventHandler {

    /**
     * 市价价格变动事件
     *  @param symbol
     *         交易对
     * @param latestPrice
     * @param third
     */
    @Override
    public void onMarketPriceChange(String symbol,
                                    BigDecimal latestPrice, boolean third) {

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

    }
}
