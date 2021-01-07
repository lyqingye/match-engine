package book;

import com.trader.core.def.OrderSide;
import com.trader.core.def.OrderType;
import com.trader.core.entity.Order;
import com.trader.core.entity.OrderBook;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author yjt
 * @since 2020/9/4 下午3:08
 */
public class TestOrderBook {

    @Test
    public void testAddOrderToBook() throws InterruptedException {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println(today);
        OrderBook book = new OrderBook();
        Date d1 = new Date();
        Thread.sleep(100);
        Date d2 = new Date();
        Thread.sleep(100);
        Date d3 = new Date();


        Order o1 = new Order();
        o1.setId("1");
        o1.setType(OrderType.MARKET);
        o1.setPrice(BigDecimal.TEN);
        o1.setSide(OrderSide.BUY);
        o1.setCreateDateTime(d1);


        Order o2 = new Order();
        o2.setId("2");
        o2.setSide(OrderSide.BUY);
        o2.setType(OrderType.MARKET);
        o2.setPrice(BigDecimal.TEN);
        o2.setCreateDateTime(d3);

        Order o3 = new Order();
        o3.setId("3");
        o3.setSide(OrderSide.BUY);
        o3.setType(OrderType.MARKET);
        o3.setPrice(BigDecimal.valueOf(11));
        o3.setCreateDateTime(d3);

        book.addOrder(o2);
        book.addOrder(o1);
        book.addOrder(o3);

        book.getAskLimitOrders().forEach(System.out::println);
        book.getBidLimitOrders().forEach(System.out::println);
    }
}
