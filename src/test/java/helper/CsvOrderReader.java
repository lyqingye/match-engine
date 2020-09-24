package helper;

import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.utils.SnowflakeIdWorker;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yjt
 * @since 2020/9/21 下午3:22
 */
public class CsvOrderReader {



    public static List<Order> read (File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        String line;
        List<Order> result = new ArrayList<>(1000000);
        int count = 0;
        while((line = randomAccessFile.readLine())!= null){
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
            order.setPrice(new BigDecimal(args[0]).setScale(8, RoundingMode.DOWN));
            order.setExecutedAmount(BigDecimal.ZERO);


            order.setPriceUpperBound(new BigDecimal(args[1]).setScale(8, RoundingMode.DOWN));
            order.setPriceLowerBound(new BigDecimal(args[2]).setScale(8, RoundingMode.DOWN));

            order.setTotalAmount(new BigDecimal(args[3]).setScale(8, RoundingMode.DOWN));
            order.setLeavesAmount(order.getTotalAmount().setScale(8, RoundingMode.DOWN));

            order.setCreateDateTime(new Date());

            order.setQuantity(new BigDecimal(args[4]).setScale(8, RoundingMode.DOWN));

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
                order.setLeavesQuantity(order.getQuantity().setScale(8, RoundingMode.DOWN));

                if (order.isMarketOrder()) {
                    order.setLeavesAmount(BigDecimal.ZERO);
                    order.setTotalAmount(BigDecimal.ZERO);
                }
            }
            result.add(order);

            System.out.println(++count);
        }
        randomAccessFile.close();
        return result;
    }
}
