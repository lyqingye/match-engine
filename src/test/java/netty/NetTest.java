package netty;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.parsetools.RecordParser;
import org.junit.Test;

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
        HttpClient client = Vertx.vertx()
                                 .createHttpClient(
                                         new HttpClientOptions()
                                                 .setLogActivity(true)
                                                 .setConnectTimeout(5000)
                                                 .setDefaultPort(8087)
                                                 .setDefaultHost("localhost")
                                 );
        client.getAbs("http://localhost:8087/market/symbol/t2g/mappings", ar -> {
            ar.bodyHandler(buf -> {
                System.out.println(buf.toString());
            });
        });
//        client.request(HttpMethod.GET, "/market/price/latest",ar -> {
//            ar.bodyHandler(buf -> {
//                System.out.println(buf.toString());
//            });
//        });

        Thread.sleep(1000000);

        System.out.println();
    }
}
