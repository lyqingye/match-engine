package com.trader.context;

import com.trader.MarketManager;
import com.trader.MatchEngine;
import com.trader.Matcher;

import java.math.BigDecimal;

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
}
