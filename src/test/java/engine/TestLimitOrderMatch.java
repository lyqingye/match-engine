package engine;

import com.trader.MatchEngine;
import com.trader.config.MatchEngineConfig;
import com.trader.core.MatchHandler;
import com.trader.core.entity.Order;
import com.trader.core.factory.OrderFactory;
import com.trader.core.matcher.TradeResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yjt
 * @since 2020/9/17 下午7:39
 */
public class TestLimitOrderMatch  {

    private MatchEngine engine;



    @Before
    public void before () {
        MatchEngineConfig config = new MatchEngineConfig();
        config.setWebsocketConfigClientHost("119.23.49.169");
        config.setHandler(new MatchHandler() {
            @Override
            public void onExecuteOrder(Order order, Order opponentOrder, TradeResult ts) throws Exception {
                // 持久化
            }
        });
        engine = MatchEngine.newEngine(config);
        engine.enableMatching();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test

    public void addOrder () {

        final long start = System.currentTimeMillis();
        List<Order> orderList = new ArrayList<>(1000000);
        for (int i = 0; i < 2; i++) {


            BigDecimal price = BigDecimal.TEN;
            Order buyLimitOrder = OrderFactory.limit()
                    .buy("1", "BTC", "USDT")
                    .spent(BigDecimal.TEN.multiply(price))
                    .withUnitPriceOf(price)
                    .quantity(BigDecimal.TEN)
                    .GTC()
                    .build();

            Order sellMarketOrder = OrderFactory.limit()
                                                .sell("2", "BTC", "USDT")
                                                .quantity(BigDecimal.TEN)
                                                .withUnitPriceOf(price)
                                                .GTC()
                                                .build();
            orderList.add(buyLimitOrder);
            orderList.add(sellMarketOrder);
        }
//        Collections.shuffle(orderList);
        for (Order order : orderList) {
            engine.addOrder(order);
        }


        final long end = System.currentTimeMillis();
//        System.out.println("process  orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");


        engine.enableMatching();

        System.out.println("startTime:" + System.currentTimeMillis());

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
