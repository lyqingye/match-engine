package netty;

import com.trader.core.def.OrderSide;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.TcpMarketPublishClient;
import com.trader.market.publish.config.MarketConfigHttpClient;
import com.trader.market.publish.msg.Message;
import com.trader.market.publish.msg.TradeMessage;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
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
        TradeMessage tm = new TradeMessage();
        tm.setDirection(OrderSide.BUY.toDirection());
        tm.setPrice(new BigDecimal("12.321313"));
        tm.setQuantity(new BigDecimal("12.321313"));
        tm.setSymbol("BTC-USDT");
        tm.setTs(System.currentTimeMillis());

        Message<TradeMessage> of1 = TradeMessage.of(tm);
        String data = Json.encode(of1);

        Buffer buf = TradeMessage.toMessageBuf(tm);

        long start = System.nanoTime();
        for (int i = 0; i < 10000000; i++) {
            JsonObject o = (JsonObject) Json.decodeValue(data);
            TradeMessage data1 = o.getJsonObject("data").mapTo(TradeMessage.class);
            Message<TradeMessage> msg = TradeMessage.of(buf);
        }

        long end = System.nanoTime();

        System.out.println(TimeUnit.NANOSECONDS.toNanos(end - start) / 10000000);
//
//        Buffer buf = TradeMessage.toMeof1.ssageBuf(tm);
//
//        Message<TradeMessage> of = Traof1.deMessage.of(buf);
//        System.out.println();of1.
//
//        System.out.println();

        BigDecimal decimal = new BigDecimal("1.23324111");
        System.out.println(decimal.doubleValue());

        String str = "01234567890";
        byte[] data2 = str.getBytes();
        Buffer buf2 = Buffer.buffer(data2.length + 4);

        buf2.appendInt(data2.length);
        buf2.appendBytes(data2);
        buf2.appendInt(20);
        buf2.appendBytes(data2);

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

        parser.handle(buf2);

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
