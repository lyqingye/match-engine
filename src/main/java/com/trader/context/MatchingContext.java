package com.trader.context;

import com.trader.Matcher;
import com.trader.market.MarketManager;
import com.trader.MatchEngine;
import com.trader.entity.OrderBook;

/**
 * @author yjt
 * @since 2020/9/18 下午4:29
 */
public interface MatchingContext {

     /**
      * 获取当前市场价
      *
      * @return 市场价
      */
     MarketManager getMarketMgr ();

     /**
      * 获取撮合引擎
      *
      * @return 撮合引擎
      */
     MatchEngine getEngine ();

     /**
      * 当前的买卖账本
      *
      * @return 账本
      */
     OrderBook getOrderBook();

     /**
      * 获取当前的匹配器
      *
      * @return 匹配器
      */
     Matcher getMatcher();

     /**
      * 获取附加参数值
      *
      * @param key 参数key
      * @param <T> 目标类型
      * @return 附加参数值
      */
     <T> T getAttribute(String key);

     /**
      * 设置附加参数值
      *
      * @param key key
      * @param value value
      */
     void setAttribute (String key,Object value);
}
