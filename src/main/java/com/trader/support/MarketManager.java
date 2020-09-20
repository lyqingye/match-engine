package com.trader.support;

import com.trader.MatchHandler;
import com.trader.entity.Currency;
import com.trader.entity.Product;

import java.math.BigDecimal;

/**
 * 行情管理器
 *
 * @author yjt
 * @since 2020/9/18 下午4:34
 */
public class MarketManager {

    /**
     * 获取产品对货币的市场价格
     *
     * 市场价格 = 商品 / 货币
     *
     * @param product  商品
     * @param currency 货币
     * @return 市场价格
     */
    public BigDecimal getMarketPrice (Product product, Currency currency) {
        return BigDecimal.ONE;
    }


    /**
     * @return
     */
    public MatchHandler getHandler () {
        return null;
    }
}
