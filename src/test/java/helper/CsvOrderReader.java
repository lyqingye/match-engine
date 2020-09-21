package helper;

import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.utils.SnowflakeIdWorker;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yjt
 * @since 2020/9/21 下午3:22
 */
public class CsvOrderReader {

    public static List<Order> read (File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<Order> result = new ArrayList<>(64);
        while((line = reader.readLine())!= null){
            // 忽略注释
            if (line.startsWith("#")) {
                continue;
            }

            String[] args = line.split(",");

            Order order = new Order();
            order.setId(SnowflakeIdWorker.nextId());
            order.setUid("1");
            order.setProductId("BTC");
            order.setCurrencyId("USDT");
            order.setPrice(new BigDecimal(args[0]));
            order.setExecutedAmount(BigDecimal.ZERO);


            order.setPriceUpperBound(new BigDecimal(args[1]));
            order.setPriceLowerBound(new BigDecimal(args[2]));

            order.setTotalAmount(new BigDecimal(args[3]));
            order.setLeavesAmount(order.getTotalAmount());

            order.setCreateDateTime(new Date());

            order.setQuantity(new BigDecimal(args[4]));

            order.setExecutedQuantity(BigDecimal.ZERO);

            if (args[5].equals(OrderSide.BUY.name())) {
                order.setSide(OrderSide.BUY);
            }
            if (args[5].equals(OrderSide.SELL.name())) {
                order.setSide(OrderSide.SELL);
            }

            order.setTimeInForce(OrderTimeInForce.GTC);
            order.setVersion(1);
            if (args[6].equals(OrderType.LIMIT.name())) {
                order.setType(OrderType.LIMIT);
            }
            if (args[6].equals(OrderType.MARKET.name())) {
                order.setType(OrderType.MARKET);
            }

            if (order.isSell()) {
                order.setLeavesQuantity(order.getQuantity());
            }

            result.add(order);
        }
        reader.close();
        return result;
    }
}
