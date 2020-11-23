package com.trader.market.publish.msg;

import com.trader.core.def.OrderSide;
import com.trader.utils.SymbolUtils;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/10/11 15:09
 */

@Data
public class TradeMessage {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 最终的成交价
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private BigDecimal quantity;

    /**
     * 成交主动方
     */
    private String direction;

    /**
     * 交易时间
     */
    private Long ts;

    public static Message<TradeMessage> of(TradeMessage ts) {
        final Message<TradeMessage> msg = new Message<>();
        msg.setType(MessageType.TRADE_RESULT);
        msg.setData(ts);
        msg.setTs(System.currentTimeMillis());
        return msg;
    }

    public static Buffer toBuf(TradeMessage ts) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | quantity (8byte) | direction (1byte) | ts (8byte)
        byte[] symbolBytes = SymbolUtils.toGenericSymbol(ts.getSymbol()).getBytes();
        int msgSize = 38 + symbolBytes.length;
        return Buffer.buffer(msgSize)
                .appendInt(msgSize)
                .appendByte((byte) MessageType.TRADE_RESULT.ordinal())
                .appendLong(System.currentTimeMillis())
                .appendInt(symbolBytes.length)
                .appendBytes(symbolBytes)
                .appendDouble(ts.getPrice().doubleValue())
                .appendDouble(ts.getQuantity().doubleValue())
                .appendByte((byte) OrderSide.toSide(ts.getDirection()).ordinal())
                .appendLong(ts.getTs());
    }

    public static TradeMessage of(Buffer buf, int readOffset,int msgSize) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | quantity (8byte) | direction (1byte) | ts (8byte)
        int offset = readOffset;
        TradeMessage ts = new TradeMessage();
        int symbolLength = buf.getInt(offset);
        if (symbolLength != msgSize - 38) {
            return null;
        }
        offset += 4;
        byte[] symbolBytes = buf.getBytes(offset, offset + symbolLength);
        offset += symbolLength;
        ts.setSymbol(new String(symbolBytes));
        ts.setPrice(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        ts.setQuantity(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        ts.setDirection(OrderSide.toSide(buf.getByte(offset)).toDirection());
        offset += 1;
        ts.setTs(buf.getLong(offset));
        return ts;
    }
}
