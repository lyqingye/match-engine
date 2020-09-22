package com.trader.market;

import com.trader.def.OrderSide;
import com.trader.entity.Product;

import java.math.BigDecimal;

/**
 * 市场事件处理器
 *
 * @author yjt
 * @since 2020/9/20 下午2:36
 */
public interface MarketEventHandler {

    /**
     * 市价价格变动事件
     *
     * @param productId 产品ID
     * @param currencyId 货币ID
     * @param latestPrice 最新价格
     */
    void onMarketPriceChange(String productId, String currencyId, BigDecimal latestPrice);

    /**
     * 交易成功事件
     *
     * @param symbolId 交易对
     * @param side 买入 / 卖出
     * @param quantity 成交数量
     * @param price 成交价格
     */
    void onTrade (String symbolId, OrderSide side, BigDecimal quantity,BigDecimal price);
}
