package queue;

import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yjt
 * @since 2020/9/24 下午4:02
 */
public class DisruptorTest {

    @Test
    public void test() throws InterruptedException {
        Set<Integer> result = new HashSet<>(10000);
        DisruptorQueue<Integer> queue = DisruptorQueueFactory.createQueue(1024, new AbstractDisruptorConsumer<Integer>() {
            @Override
            public void process(Integer event) {
                result.add(event);
            }
        });


        for (int j = 0; j < 10; j++) {
            int finalJ = j;
            new Thread(() -> {
                final int size = finalJ * 1000 + 1000;

                for (int i = 1000 * finalJ; i < size; i++) {
                    queue.add(i);
                }
            }).start();
        }




        while (result.size() < 10000) {
            Thread.sleep(10);
        }

        System.out.println();

        Thread.sleep(100000);
    }
}
