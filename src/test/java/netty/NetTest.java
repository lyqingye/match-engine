package netty;

import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.TcpMarketPublishClient;
import com.trader.market.publish.config.MarketConfigHttpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.parsetools.RecordParser;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yjt
 * @since 2020/10/10 下午7:41
 */
public class NetTest {

    final Vertx vertx = Vertx.vertx();

    @Test

    public void test() {
        String str = "01234567890";
        byte[] data = str.getBytes();
        Buffer buf = Buffer.buffer(data.length + 4);

        buf.appendInt(data.length);
        buf.appendBytes(data);
        buf.appendInt(20);
        buf.appendBytes(data);

        RecordParser parser = RecordParser.newFixed(4);

        AtomicInteger size = new AtomicInteger(-1);
        parser.setOutput(h -> {

            if (size.get() == -1) {
                size.set(h.getInt(0));
                parser.fixedSizeMode(size.get());
            } else {
                System.out.println(h.toString());
                parser.fixedSizeMode(4);
                size.set(-1);
            }
        });

        parser.handle(buf);

//        System.out.println(buf.toString());

    }


    @Test
    public void testHttp() throws InterruptedException {
        TcpMarketPublishClient tcpMarketPublishClient = new TcpMarketPublishClient("localhost", 8888);
        MarketConfigHttpClient configClient = tcpMarketPublishClient.createConfigClient("localhost", 8087);
        new MarketPublishHandler(tcpMarketPublishClient);

        Thread.sleep(5000);
        configClient.updateMarketPriceSync("OEG-USDT", BigDecimal.ONE);
//        MatchEngine engine = new MatchEngine();
//        engine.getMarketMgr()
//              .tryToInitMarketPrice(configClient::getMarketPriceSync);

//        Map<String, String> price = configClient.getMarketPriceSync();
//        System.out.println(price);
        Thread.sleep(1000000);

        System.out.println();
    }

    @Test
    public void beanchmarkWebsocket() {
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(20001);
        for (int i = 0; i < 20000; i++) {
            vertx.createHttpClient()
                 .webSocket(8089, "192.168.1.55", "/", ar -> {
                     if (ar.succeeded()) {
                         WebSocket ws = ar.result();
                         ws.writeTextMessage("{\n" +
                                                     "  \"sub\": \"market.btcusdt.detail\",\n" +
                                                     "  \"id\": \"id1\"\n" +
                                                     "}");
                         countDownLatch.countDown();
//                         ws.frameHandler(f -> {
//                             if (f.isFinal() && f.isText()) {
//                                 System.out.println(f.textData());
//                             }
//                         });
                     }
                 });
        }


        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(TimeUnit.MILLISECONDS.toSeconds(end - start));
        System.out.println();
    }
}
