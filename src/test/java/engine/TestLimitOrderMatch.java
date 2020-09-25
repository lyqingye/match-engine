package engine;

import com.trader.MatchEngine;
import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Currency;
import com.trader.entity.Order;
import com.trader.entity.Product;
import com.trader.matcher.limit.InMemoryLimitMatchHandler;
import com.trader.handler.ExampleLoggerHandler;
import com.trader.matcher.limit.LimitOrderMatcher;
import com.trader.matcher.market.InMemoryMarketMatchHandler;
import com.trader.matcher.market.MarketOrderMatcher;
import com.trader.utils.SnowflakeIdWorker;
import helper.CsvOrderReader;
import junit.extensions.RepeatedTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * @author yjt
 * @since 2020/9/17 下午7:39
 */
public class TestLimitOrderMatch  {

    private MatchEngine engine;



    @Before
    public void before () {
        engine = new MatchEngine();
        engine.addHandler(new InMemoryLimitMatchHandler());
        engine.addHandler(new InMemoryMarketMatchHandler());
//        engine.addHandler(new ExampleLoggerHandler());

        engine.addMatcher(new LimitOrderMatcher());
        engine.addMatcher(new MarketOrderMatcher());

        engine.enableMatching();
    }

    @Test

    public void addOrder () {
        ConcurrentSkipListSet<Order> skipListSet = new ConcurrentSkipListSet<>();
        try {
            List<Order> orderList = CsvOrderReader.read(new File("/home/ex/桌面/order3.csv"));
            System.out.println("read complete start process orders count: " + orderList.size());
            Collections.shuffle(orderList);
            long start = System.currentTimeMillis();
            for (Order order : orderList) {
                engine.addOrder(order);
            }
            long end = System.currentTimeMillis();
            System.out.println("process " + orderList.size() + " orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void after () {
    }
}
