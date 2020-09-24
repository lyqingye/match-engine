package queue;

import com.trader.entity.Order;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import org.junit.Test;

/**
 * @author yjt
 * @since 2020/9/24 下午4:02
 */
public class DisruptorTest {

    @Test
    public void test() throws InterruptedException {
        DisruptorQueue<Order> queue = DisruptorQueueFactory.createQueue(1024, new AbstractDisruptorConsumer<Order>() {
            @Override
            public void process(Order order) {
                System.out.println(order.getId());
            }
        });

        for (int i = 0; i < 100; i++) {
            Order order = new Order();
            order.setId(String.valueOf(i));
            queue.add(order);
        }

        Thread.sleep(100000);
    }
}
