package com.trader.market.publish.msg;

import com.trader.utils.SymbolUtils;
import io.vertx.core.buffer.Buffer;
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
    @Deprecated
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
        msg.setTs(System.currentTimeMillis());
        return msg;
    }

    public static Buffer toBuf(PriceChangeMessage pc) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | third (1byte)
        byte[] symbolBytes = SymbolUtils.toGenericSymbol(pc.getSymbol()).getBytes();
        int msgSize = 22 + symbolBytes.length;
        return Buffer.buffer(msgSize)
                .appendInt(msgSize)
                .appendByte((byte) MessageType.MARKET_PRICE.ordinal())
                .appendLong(System.currentTimeMillis())
                .appendInt(symbolBytes.length)
                .appendBytes(symbolBytes)
                .appendDouble(pc.getPrice().doubleValue())
                .appendByte((byte) (Boolean.TRUE.equals(pc.getThird()) ? 1 : 0));
    }

    public static PriceChangeMessage of(Buffer buf, int readOffset, int msgSize) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | third (1byte)
        int offset = readOffset;
        PriceChangeMessage pc = new PriceChangeMessage();
        int symbolLength = buf.getInt(offset);
        if (symbolLength != msgSize - 22) {
            return null;
        }
        offset += 4;
        byte[] symbolBytes = buf.getBytes(offset, offset + symbolLength);
        offset += symbolLength;
        pc.setSymbol(new String(symbolBytes));
        pc.setPrice(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        byte third = buf.getByte(offset);
        pc.setThird(third == 1);
        return pc;
    }
}
