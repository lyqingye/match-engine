package engine;

import com.trader.MatchEngine;
import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.factory.OrderFactory;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.TcpMarketPublishClient;
import com.trader.matcher.limit.InMemoryLimitMatchHandler;
import com.trader.handler.ExampleLoggerHandler;
import com.trader.matcher.limit.LimitOrderMatcher;
import com.trader.matcher.market.InMemoryMarketMatchHandler;
import com.trader.matcher.market.MarketOrderMatcher;
import com.trader.utils.SnowflakeIdWorker;
import helper.CsvOrderReader;
import junit.extensions.RepeatedTest;
import org.apache.commons.lang3.RandomUtils;
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
//        engine.enableLog();

        engine.addMatcher(new LimitOrderMatcher());
        engine.addMatcher(new MarketOrderMatcher());
        engine.enableMatching();

        engine.getMarketMgr()
              .addHandler(new MarketPublishHandler(new TcpMarketPublishClient("192.168.1.61", 8888)));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test

    public void addOrder () {

        final long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            System.out.println("process " + i);
            BigDecimal price = BigDecimal.valueOf(RandomUtils.nextDouble(0.1, 10))
                                         .setScale(8, BigDecimal.ROUND_DOWN);
            Order buyLimitOrder = OrderFactory.limit()
                                              .buy("1", "BTC-USDT")
                                              .spent(BigDecimal.TEN.multiply(price))
                                              .withUnitPriceOf(price)
                                              .quantity(BigDecimal.TEN)
                                              .withUnitPriceCap(BigDecimal.valueOf(0.1))
                                              .GTC()
                                              .build();

            Order sellLimitOrder = OrderFactory.limit()
                                               .sell("2", "BTC-USDT")
                                               .quantity(BigDecimal.valueOf(100))
                                               .withUnitPriceOf(BigDecimal.valueOf(RandomUtils.nextDouble(0.1, 10)).setScale(8, BigDecimal.ROUND_DOWN))
                                               .GTC()
                                               .build();
            engine.addOrder(buyLimitOrder);
            engine.addOrder(sellLimitOrder);

        }
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long end = System.currentTimeMillis();
            System.out.println("process  orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");


//
//
//
//
//        try {
//            Thread.sleep(1000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println();
//        ConcurrentSkipListSet<Order> skipListSet = new ConcurrentSkipListSet<>();
//        try {
//            List<Order> orderList = CsvOrderReader.read(new File("/home/ex/桌面/order.csv"));
//                System.out.println("read complete start process orders count: " + orderList.size());
//            Collections.shuffle(orderList);
//            long start = System.currentTimeMillis();
//            for (Order order : orderList) {
//                engine.addOrder(order);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("process " + orderList.size() + " orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");
//            System.out.println();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @After
    public void after () {
    }
}
