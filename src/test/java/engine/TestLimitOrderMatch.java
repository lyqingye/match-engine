package engine;

import com.trader.MatchEngine;
import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Currency;
import com.trader.entity.Order;
import com.trader.entity.Product;
import com.trader.matcher.limit.InMemoryLimitMatchHandler;
import com.trader.handler.LogMatchHandler;
import com.trader.matcher.limit.LimitOrderMatcher;
import com.trader.utils.SnowflakeIdWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yjt
 * @since 2020/9/17 下午7:39
 */
public class TestLimitOrderMatch {

    private MatchEngine engine;

    @Before
    public void before () {
        engine = new MatchEngine();
        engine.addHandler(new InMemoryLimitMatchHandler());
        engine.addMatcher(new LimitOrderMatcher());
        engine.addHandler(new LogMatchHandler());
        engine.addProduct(new Product("BTC","BTC"));
        engine.addCurrency(new Currency("USDT","USDT"));
        engine.enableMatching();
    }

    @Test
    public void addOrder () {

        // 单价10 买入 100个
        Order buy1 = new Order();
        buy1.setId(SnowflakeIdWorker.nextId());
        buy1.setPrice(BigDecimal.valueOf(10));
        buy1.setQuantity(BigDecimal.valueOf(100));
        buy1.setLeavesQuantity(BigDecimal.valueOf(100));
        buy1.setProductId("BTC");
        buy1.setCurrencyId("USDT");
        buy1.setType(OrderType.LIMIT);
        buy1.setTimeInForce(OrderTimeInForce.GTC);
        buy1.setUid("1");
        buy1.setSide(OrderSide.BUY);
        buy1.setCreateDateTime(new Date());


        // 单价11 买入 100个
        Order buy2 = new Order();
        buy2.setId(SnowflakeIdWorker.nextId());
        buy2.setPrice(BigDecimal.valueOf(11));
        buy2.setQuantity(BigDecimal.valueOf(100));
        buy2.setLeavesQuantity(BigDecimal.valueOf(100));
        buy2.setProductId("BTC");
        buy2.setCurrencyId("USDT");
        buy2.setType(OrderType.LIMIT);
        buy2.setTimeInForce(OrderTimeInForce.GTC);
        buy2.setUid("1");
        buy2.setSide(OrderSide.BUY);
        buy2.setCreateDateTime(new Date());

        // 单价12 买入 10个
        Order buy3 = new Order();
        buy3.setId(SnowflakeIdWorker.nextId());
        buy3.setPrice(BigDecimal.valueOf(12));
        buy3.setQuantity(BigDecimal.valueOf(10));
        buy3.setLeavesQuantity(BigDecimal.valueOf(10));
        buy3.setProductId("BTC");
        buy3.setCurrencyId("USDT");
        buy3.setType(OrderType.LIMIT);
        buy3.setTimeInForce(OrderTimeInForce.GTC);
        buy3.setUid("1");
        buy3.setSide(OrderSide.BUY);
        buy3.setCreateDateTime(new Date());


        // 单价10 卖出 90个
        Order sell1 = new Order();
        sell1.setId(SnowflakeIdWorker.nextId());
        sell1.setPrice(BigDecimal.valueOf(10));
        sell1.setQuantity(BigDecimal.valueOf(90));
        sell1.setLeavesQuantity(BigDecimal.valueOf(90));
        sell1.setProductId("BTC");
        sell1.setCurrencyId("USDT");
        sell1.setType(OrderType.LIMIT);
        sell1.setTimeInForce(OrderTimeInForce.GTC);
        sell1.setUid("1");
        sell1.setSide(OrderSide.SELL);
        sell1.setCreateDateTime(new Date());

        // 单价11 卖出 100个
        Order sell2 = new Order();
        sell2.setId(SnowflakeIdWorker.nextId());
        sell2.setPrice(BigDecimal.valueOf(11));
        sell2.setQuantity(BigDecimal.valueOf(100));
        sell2.setLeavesQuantity(BigDecimal.valueOf(100));
        sell2.setProductId("BTC");
        sell2.setCurrencyId("USDT");
        sell2.setType(OrderType.LIMIT);
        sell2.setTimeInForce(OrderTimeInForce.GTC);
        sell2.setUid("1");
        sell2.setSide(OrderSide.SELL);
        sell2.setCreateDateTime(new Date());

        // 单价10 卖出 10个
        Order sell3 = new Order();
        sell3.setId(SnowflakeIdWorker.nextId());
        sell3.setPrice(BigDecimal.valueOf(10));
        sell3.setQuantity(BigDecimal.valueOf(10));
        sell3.setLeavesQuantity(BigDecimal.valueOf(10));
        sell3.setProductId("BTC");
        sell3.setCurrencyId("USDT");
        sell3.setType(OrderType.LIMIT);
        sell3.setTimeInForce(OrderTimeInForce.GTC);
        sell3.setUid("1");
        sell3.setSide(OrderSide.SELL);
        sell3.setCreateDateTime(new Date());


        engine.addLimitOrder(buy1);
        engine.addLimitOrder(buy2);
        engine.addLimitOrder(buy3);
        engine.addLimitOrder(sell1);
        engine.addLimitOrder(sell2);
        engine.addLimitOrder(sell3);
    }

    @After
    public void after () {
    }

}
