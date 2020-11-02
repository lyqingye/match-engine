package com.trader.market.publish.msg;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/10/13 上午9:00
 */
@Data
public class PriceChangeMessage {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 是否为第三方数据
     */
    private Boolean third;

    public static Message<PriceChangeMessage> ofLocal(String symbol,
                                                      BigDecimal price) {
        return of(symbol, price, false);
    }

    public static Message<PriceChangeMessage> ofThird(String symbol,
                                                      BigDecimal price) {
        return of(symbol, price, true);
    }

    private static Message<PriceChangeMessage> of(String symbol,
                                                  BigDecimal price,
                                                  boolean third) {
        PriceChangeMessage data = new PriceChangeMessage();
        data.setSymbol(symbol);
        data.setPrice(price);
        data.setThird(third);
        Message<PriceChangeMessage> msg = new Message<>();
        msg.setType(MessageType.MARKET_PRICE);
        msg.setData(data);
        return msg;
    }
}
