package com.trader.context;

import com.trader.Matcher;
import com.trader.entity.OrderBook;
import com.trader.market.MarketManager;
import com.trader.utils.ThreadLocalUtils;

/**
 * @author yjt
 * @since 2020/9/22 上午9:27
 */
public class ThreadLocalMatchingContext implements MatchingContext {

    /**
     * 静态实例
     */
    public static final ThreadLocalMatchingContext INSTANCE = new ThreadLocalMatchingContext();

    /**
     * 该对象在本地线程缓存中的名称
     */
    public static final String NAME_OF_CONTEXT = "match.trade.context";

    /**
     * 市场管理器在本地线程缓存中的名称
     */
    public static final String NAME_OF_MARKET_MANAGER = "match.trade.market.manager";

    /**
     * 撮合引擎在本地线程缓存中的名称
     */
    public static final String NAME_OF_MATCH_ENGINE = "match.trade.engine";

    /**
     * 账本在本地线程缓存中的名称
     */
    public static final String NAME_OF_ORDER_BOOK = "match.trade.order.book";

    /**
     * 匹配器在本地线程缓存中的名称
     */
    public static final String NAME_OF_MATCHER = "match.trade.matcher";

    /**
     * 获取当前市场价
     *
     * @return 市场价
     */
    @Override
    public MarketManager getMarketMgr() {
        return ThreadLocalUtils.get(NAME_OF_MARKET_MANAGER);
    }

    /**
     * 当前的买卖账本
     *
     * @return 账本
     */
    @Override
    public OrderBook getOrderBook() {
        return ThreadLocalUtils.get(NAME_OF_ORDER_BOOK);
    }

    /**
     * 获取当前的匹配器
     *
     * @return 匹配器
     */
    @Override
    public Matcher getMatcher() {
        return ThreadLocalUtils.get(NAME_OF_MATCHER);
    }

    /**
     * 获取附加参数值
     *
     * @param key
     *         参数key
     *
     * @return 附加参数值
     */
    @Override
    public <T> T getAttribute(String key) {
        return ThreadLocalUtils.get(key);
    }

    /**
     * 设置附加参数值
     *
     * @param key
     *         key
     * @param value
     *         value
     */
    @Override
    public void setAttribute(String key, Object value) {
        ThreadLocalUtils.set(key, value);
    }
}
