package engine;

import com.trader.MatchEngine;
import com.trader.config.MatchEngineConfig;
import com.trader.core.MatchHandler;
import com.trader.core.entity.Order;
import com.trader.core.factory.OrderFactory;
import com.trader.core.matcher.MatchResult;
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
        config.setWebsocketConfigClientHost("localhost");
        config.setMarketPublishClientHost("localhost");

        config.setHandler(new MatchHandler() {
            @Override
            public void onExecuteOrder(Order order, Order opponentOrder, MatchResult ts) throws Exception {
                // 持久化
                System.out.println(ts);
            }
        });
        engine = MatchEngine.newEngine(config);
        engine.enableMatching();
        engine.getMarketMgr().getMarketConfigClient().putSymbolMappingSync("DOC-USDT");

//        try {
//            Thread.sleep(1000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test

    public void addOrder () {

        final long start = System.currentTimeMillis();
        List<Order> orderList = new ArrayList<>(100);
        for (int i = 0; i < 0; i++) {


            BigDecimal price = BigDecimal.TEN;
            Order buyLimitOrder = OrderFactory.limit()
                    .buy("1", "VTV", "USDT")
                    .spent(new BigDecimal("1406.02801895"))
                    .withUnitPriceOf(new BigDecimal("16369.97953393").add(BigDecimal.valueOf(i)))
                    .quantity(BigDecimal.TEN)
                    .GTC()
                    .build();

            Order sellMarketOrder = OrderFactory.limit()
                    .sell("2", "VTV", "USDT")
                    .quantity(new BigDecimal("0.08589064"))
                    .withUnitPriceOf(new BigDecimal("16369.97953393").add(BigDecimal.valueOf(i)))
                    .GTC()
                    .build();
            engine.addOrder(buyLimitOrder);
            engine.addOrder(sellMarketOrder);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        Collections.shuffle(orderList);
//        for (Order order : orderList) {
//            engine.addOrder(order);
//        }


        final long end = System.currentTimeMillis();
//        System.out.println("process  orders using " + TimeUnit.MILLISECONDS.toSeconds(end - start) + "s");


        engine.enableMatching();

        System.out.println("startTime:" + System.currentTimeMillis());

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        engine.shutdown();
        System.out.println("shutdown");
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
