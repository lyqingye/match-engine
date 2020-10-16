package com.trader.market.publish.msg;

import com.trader.matcher.TradeResult;
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
        return msg;
    }
}
