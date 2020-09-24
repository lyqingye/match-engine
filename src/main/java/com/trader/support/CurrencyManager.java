package com.trader.support;

import com.trader.entity.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/9/20 上午10:50
 */
public class CurrencyManager {

    /**
     * 货币映射MAP
     */
    private Map<String, Currency> currencyMap = new HashMap<>(16);

    /**
     * 添加一个货币
     *
     * @param currency
     *         货币对象
     */
    public void addCurrency(Currency currency) {
        Objects.requireNonNull(currency);
        currencyMap.put(currency.getId(), currency);
    }

    /**
     * 根据货币ID获取货币对象
     *
     * @param currencyId
     *         货币ID
     *
     * @return 货币对象
     */
    public Currency getCurrency(String currencyId) {
        return this.currencyMap.get(currencyId);
    }
}
