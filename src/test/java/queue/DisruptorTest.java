package queue;

import com.trader.core.exception.MatchExceptionHandler;
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
        DisruptorQueue<Integer> queue = DisruptorQueueFactory.createQueue(1024, new AbstractDisruptorConsumer<Integer>() {
            /**
             * 进行数据处理
             *
             * @param event 事件
             */
            @Override
            public void process(Integer event) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("processed");
            }
        }, MatchExceptionHandler.defaultHandler().toDisruptorHandler());
        System.out.println(queue.isEmpty());
        queue.add(1);

        while (true) {
            Thread.sleep(1000);
            System.out.println(queue.isEmpty());
        }
    }
}
