package netty;

import com.trader.market.subcribe.ThirdPartMarketSubscribe;
import com.trader.market.thrid.MarketPublishClient;
import io.netty.channel.Channel;
import org.junit.Test;

/**
 * @author yjt
 * @since 2020/10/10 下午7:41
 */
public class NettyTest {

    @Test
    public void test () {
        try {
            MarketPublishClient client = new MarketPublishClient();


//            new Thread(() -> {
//                while (true) {
//                    Channel channel = client.getChannel();
//
//                    if (channel == null) {
//                        channel.writeAndFlush(System.currentTimeMillis());
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }).start();
//
//            new Thread(() -> {
//                while (true) {
//                    Channel channel = client.getChannel();
//
//                    if (channel == null || !channel.isOpen()) {
//                        try {
//                            client.start("localhost",8888);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }).start();
            client.start("localhost",8888);

        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
