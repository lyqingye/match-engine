package scheduler;

import com.trader.book.support.router.GenericOrderRouter;
import com.trader.book.support.scheduler.GenericScheduler;
import com.trader.def.Category;
import com.trader.entity.Order;
import com.trader.factory.OrderFactory;
import com.trader.handler.ExampleLoggerHandler;
import com.trader.market.MarketManager;
import com.trader.matcher.MatcherManager;
import com.trader.support.OrderBookManager;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/10/25 16:55
 */

public class GenericSchedulerTest {

    @Test
    public void testProcessorMapping () {
        final GenericScheduler scheduler = new GenericScheduler(new GenericOrderRouter(),
                                                                new MatcherManager(),
                                                                new MarketManager(new OrderBookManager()),
                                                                new ExampleLoggerHandler(), 4);
        final Order order = OrderFactory.limit()
                                        .buy("1", "BTC", "USDT")
                                        .GTC()
                                        .quantity(BigDecimal.TEN)
                                        .spent(BigDecimal.TEN)
                                        .withUnitPriceOf(BigDecimal.ONE)
                                        .build();


        final Order order1 = OrderFactory.limit()
                                        .buy("1", "ETH", "USDT")
                                        .GTC()
                                        .quantity(BigDecimal.TEN)
                                        .spent(BigDecimal.TEN)
                                        .withUnitPriceOf(BigDecimal.ONE)
                                        .build();

        final Order order2 = OrderFactory.limit()
                                         .buy("1", "OEG", "USDT")
                                         .GTC()
                                         .quantity(BigDecimal.TEN)
                                         .spent(BigDecimal.TEN)
                                         .withUnitPriceOf(BigDecimal.ONE)
                                         .build();

        final Order order3 = OrderFactory.limit()
                                         .buy("1", "SUV", "USDT")
                                         .GTC()
                                         .quantity(BigDecimal.TEN)
                                         .spent(BigDecimal.TEN)
                                         .withUnitPriceOf(BigDecimal.ONE)
                                         .build();

        final Order order4 = OrderFactory.limit()
                                         .buy("1", "KO", "USDT")
                                         .GTC()
                                         .quantity(BigDecimal.TEN)
                                         .spent(BigDecimal.TEN)
                                         .withUnitPriceOf(BigDecimal.ONE)
                                         .build();
        final Order order5 = OrderFactory.limit()
                                         .buy("1", "JPT", "USDT")
                                         .GTC()
                                         .quantity(BigDecimal.TEN)
                                         .spent(BigDecimal.TEN)
                                         .withUnitPriceOf(BigDecimal.ONE)
                                         .build();

        scheduler.submit(order);
        scheduler.submit(order1);
        scheduler.submit(order2);
        scheduler.submit(order3);
        scheduler.submit(order4);

        order5.setCategory(Category.BOT);

        scheduler.submit(order5);
        scheduler.submit(order5);

        System.out.println();
    }
}
