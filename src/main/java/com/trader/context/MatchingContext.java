package com.trader.context;

import com.trader.support.MarketManager;
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
}
