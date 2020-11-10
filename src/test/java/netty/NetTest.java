package netty;

import com.trader.core.def.OrderSide;
import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.entity.MarketDepthInfo;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.TcpMarketPublishClient;
import com.trader.market.publish.config.MarketConfigHttpClient;
import com.trader.market.publish.msg.DepthChartMessage;
import com.trader.market.publish.msg.Message;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.market.publish.msg.TradeMessage;
import com.trader.utils.messages.FrameParser;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


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


        Buffer tmBuf = TradeMessage.toBuf(tm);
        new FrameParser((ar -> {
            System.out.println();
        })).handle(tmBuf);

        Message<PriceChangeMessage> pc = PriceChangeMessage.ofLocal("BTC-USDT", BigDecimal.valueOf(1.31332));
        Buffer pcBuf = PriceChangeMessage.toBuf(pc.getData());
        new FrameParser(ar -> {
            System.out.println();
        }).handle(pcBuf);

        MarketDepthChartSeries series = new MarketDepthChartSeries();
        series.setSymbol("BTC-USDT");
        MarketDepthChart chart = new MarketDepthChart();
        chart.setDepth(DepthLevel.step0);
        MarketDepthInfo depthInfo = new MarketDepthInfo();
        depthInfo.setPrice(BigDecimal.valueOf(1.313));
        depthInfo.setLeaves(BigDecimal.valueOf(.213123));
        depthInfo.setExecuted(BigDecimal.valueOf(2.313));
        depthInfo.setTotal(BigDecimal.valueOf(123.3123));
        chart.setAsks(Arrays.asList(depthInfo, depthInfo, depthInfo));
        chart.setBids(Arrays.asList(depthInfo, depthInfo, depthInfo));
        series.setSeries(Arrays.asList(chart, chart));
        Buffer cmBuf = DepthChartMessage.toBuf(series);

        new FrameParser(ar -> {
            System.out.println();
        }).handle(cmBuf);
        for (; ; ) {

        }

//
//        Buffer buf = TradeMessage.toMeof1.ssageBuf(tm);
//
//        Message<TradeMessage> of = Traof1.deMessage.of(buf);
//        System.out.println();of1.
//
//        System.out.println();

//        BigDecimal decimal = new BigDecimal("1.23324111");
//        System.out.println(decimal.doubleValue());
//
//        String str = "01234567890";
//        byte[] data2 = str.getBytes();
//        Buffer buf2 = Buffer.buffer(data2.length + 4);
//
//        buf2.appendInt(data2.length);
//        buf2.appendBytes(data2);
//        buf2.appendInt(20);
//        buf2.appendBytes(data2);
//
//        RecordParser parser = RecordParser.newFixed(4);
//
//        AtomicInteger size = new AtomicInteger(-1);
//        parser.setOutput(h -> {
//
//            if (size.get() == -1) {
//                size.set(h.getInt(0));
//                parser.fixedSizeMode(size.get());
//            } else {
//                System.out.println(h.toString());
//                parser.fixedSizeMode(4);
//                size.set(-1);
//            }
//        });
//
//        parser.handle(buf2);

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
