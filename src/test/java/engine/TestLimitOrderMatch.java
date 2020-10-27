package engine;

import com.trader.MatchEngine;
import com.trader.entity.Order;
import com.trader.factory.OrderFactory;
import com.trader.handler.ExampleLoggerHandler;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.TcpMarketPublishClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author yjt
 * @since 2020/9/17 下午7:39
 */
public class TestLimitOrderMatch  {

    private MatchEngine engine;



    @Before
    public void before () {
        engine = MatchEngine.newEngine(4, 1 << 18,
                                       1 << 18,
                                       new ExampleLoggerHandler());
        engine.enableMatching();
        engine.disableMatching();

        engine.getMarketMgr()
              .addHandler(new MarketPublishHandler(new TcpMarketPublishClient("localhost", 8888)));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test

    public void addOrder () {


        for (int i = 0; i < 100000; i++) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("process " + i);
//            BigDecimal price = engine.getMarketMgr().getMarketPrice("BTC-USDT");
            BigDecimal price = BigDecimal.TEN;
            Order buyLimitOrder = OrderFactory.limit()
                                              .buy("1", "BTC", "USDT")
                                              .spent(BigDecimal.TEN.multiply(price))
                                              .withUnitPriceOf(price)
                                              .quantity(BigDecimal.TEN)
                                              .GTC()
                                              .build();

            Order sellMarketOrder = OrderFactory.limit()
                                                .sell("1", "BTC", "USDT")
                                                .quantity(BigDecimal.TEN)
                                                .withUnitPriceOf(price)
                                                .GTC()
                                                .build();
            engine.addOrder(buyLimitOrder);
            engine.addOrder(sellMarketOrder);

        }

        final long start = System.currentTimeMillis();

        engine.enableMatching();

        final long end = System.currentTimeMillis();
        System.out.println("process  orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
