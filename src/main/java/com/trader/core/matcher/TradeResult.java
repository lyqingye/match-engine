package com.trader.core.matcher;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/18 下午2:18
 */
@Data
public class TradeResult {
    /**
     * 最终的成交价
     */
    private BigDecimal executePrice;

    /**
     * 成交量
     */
    private BigDecimal quantity;

    /**
     * 如果当前订单为买单, 则表示花费计价货币
     * 如果当前订单为卖单, 则表示收到的计价货币
     */
    private BigDecimal executeAmount;

    /**
     * 对手订单最终的成交价
     */
    private BigDecimal opponentExecutePrice = BigDecimal.ZERO;

    /**
     * 如果对手订单为买单, 则表示花费计价货币
     * 如果对手订单为卖单, 则表示收到的计价货币
     */
    private BigDecimal opponentExecuteAmount = BigDecimal.ZERO;

    /**
     * 交易时间
     */
    private long timestamp;

    /**
     * 差价
     */
    private BigDecimal diffPrice = BigDecimal.ZERO;

    /**
     * 对手订单差价
     */
    private BigDecimal opponentDiffPrice = BigDecimal.ZERO;

    /**
     * 平台吃到的差价
     */
    private BigDecimal platformDiffPrice = BigDecimal.ZERO;
}
