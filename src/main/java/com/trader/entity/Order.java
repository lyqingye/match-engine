package com.trader.entity;

import com.trader.def.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;

/**
 * 订单的定义
 *
 * @author yjt
 * @since 2020/9/1 上午9:21
 */
@Getter
@Setter
public class Order {

    /**
     * 订单ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 产品ID
     */
    private String productId;

    /**
     * 货币ID
     */
    private String currencyId;

    /**
     * 订单类型:
     * + 市价
     * + 限价
     * + 止盈止损
     */
    private OrderType type;

    /**
     * 买入或卖出
     */
    private OrderSide side;

    /**
     * 差价策略
     */
    private DifferencePriceStrategy differencePriceStrategy = DifferencePriceStrategy.BUYER_FIRST;

    /**
     * 价格
     */
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 上限 (非市价单)
     */
    private BigDecimal priceUpperBound = BigDecimal.ZERO;

    /**
     * 下界 (非市价单)
     */
    private BigDecimal priceLowerBound = BigDecimal.ZERO;

    /**
     * 触发价 (止盈止损)
     */
    private BigDecimal triggerPrice = BigDecimal.ZERO;

    /**
     * 数量
     */
    private BigDecimal quantity = BigDecimal.ZERO;

    /**
     * 已经执行的数量 （买入/卖出）的数量
     */
    private BigDecimal executedQuantity = BigDecimal.ZERO;

    /**
     * 剩余数量
     */
    private BigDecimal leavesQuantity = BigDecimal.ZERO;

    /**
     * 总金额 （市价买单 | 限价买单）
     */
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 已经执行的金额 （市价买单 | 限价买单）
     */
    private BigDecimal executedAmount = BigDecimal.ZERO;

    /**
     * 剩余执行的金额 （市价买单 | 限价买单）
     */
    private BigDecimal leavesAmount = BigDecimal.ZERO;

    /**
     * 成交方式:
     * + 部分成交直到订单取消
     * + 只允许全部成交
     * + ...
     */
    private OrderTimeInForce timeInForce;

    /**
     * 创建时间
     */
    private Date createDateTime;

    /**
     * 版本 (预留)
     */
    private long version;

    public boolean isBuy () {
        return OrderSide.BUY.equals(side);
    }

    public boolean isSell () {
        return OrderSide.SELL.equals(side);
    }

    public boolean isFOK () {
        return OrderTimeInForce.FOK.equals(this.timeInForce);
    }

    public boolean isAON () {
        return OrderTimeInForce.AON.equals(this.timeInForce);
    }

    public boolean isLimitOrder () {
        return OrderType.LIMIT.equals(this.type);
    }

    public boolean isMarketOrder () {
        return OrderType.MARKET.equals(this.type);
    }

    public boolean isStopOrder () {
        return OrderType.STOP.equals(this.type);
    }

    public boolean isBuyMarketOrder () {
        return this.isBuy() && this.isMarketOrder();
    }

    public boolean isSellMarketOrder () {
        return this.isSell() && this.isMarketOrder();
    }

    /**
     * 获取当前订单的最高买入价或最低卖出价
     *
     * @return
     */
    public BigDecimal getBoundPrice() {

        // 市价单没有上界和下限
        if (this.isMarketOrder()) {
            throw new IllegalArgumentException("订单为市价单,不存在上届下限的价格");
        }

        //
        // 买入订单是上界
        // 卖出订单是下限
        //
        if (this.isBuy()) {
            return price.add(price.multiply(this.priceUpperBound))
                        .setScale(8, RoundingMode.DOWN);
        }else {
            return price.subtract(price.multiply(this.priceLowerBound))
                        .setScale(8,RoundingMode.DOWN);
        }
    }

    /**
     * 是否存在上界下限价格
     *
     * @return 是否存在上界下限价格
     */
    public boolean hasBoundPrice () {
        // 市价单没有上界和下限
        if (isMarketOrder()) {
            throw new IllegalArgumentException("[市价]订单不存在上界下限的价格");
        }

        if (this.isBuy()) {
            return this.priceUpperBound.compareTo(BigDecimal.ZERO) == 0;
        }else {
            return this.priceLowerBound.compareTo(BigDecimal.ZERO) == 0;
        }
    }

    public BigDecimal decLeavesQuality (BigDecimal q) {
        this.leavesQuantity = leavesQuantity.subtract(q);
        return this.leavesQuantity;
    }

    public BigDecimal incExecutedQuality (BigDecimal q) {
        this.executedQuantity = executedQuantity.add(q);
        return this.executedQuantity;
    }

    public BigDecimal decLeavesAmount (BigDecimal q) {
        this.leavesAmount = leavesAmount.subtract(q);
        return this.leavesAmount;
    }

    public BigDecimal incExecutedAmount (BigDecimal q) {
        this.executedAmount = executedAmount.add(q);
        return this.executedAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(version,order.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,version);
    }

    @Override
    public Order clone() {
        Order order = new Order();
        order.id = id;
        order.uid = uid;
        order.productId = productId;
        order.price = price;

        order.totalAmount = totalAmount;
        order.executedAmount = executedAmount;
        order.leavesAmount = leavesAmount;

        order.priceUpperBound = priceUpperBound;
        order.priceLowerBound = priceLowerBound;

        order.createDateTime = createDateTime;
        order.quantity = quantity;
        order.leavesQuantity = leavesQuantity;
        order.executedQuantity = executedQuantity;

        order.side = side;
        order.type = type;
        order.timeInForce = timeInForce;
        order.version = version;
        return order;
    }

    public Order snap () {
        return this.clone();
    }

    public void rollback(Order o) {
        Objects.requireNonNull(o);
        this.id = o.id;
        this.uid = o.uid;
        this.productId = o.productId;
        this.price = o.price;
        this.createDateTime = o.createDateTime;

        this.totalAmount = o.totalAmount;
        this.executedAmount = o.executedAmount;
        this.leavesAmount = o.leavesAmount;

        this.priceUpperBound = o.priceUpperBound;
        this.priceLowerBound = o.priceLowerBound;

        this.quantity = o.quantity;
        this.executedQuantity = o.executedQuantity;
        this.leavesQuantity = o.leavesQuantity;

        this.side = o.side;
        this.type = o.type;
        this.timeInForce = o.timeInForce;
        this.version = o.version;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", productId='" + productId + '\'' +
                ", type=" + type +
                ", side=" + side +
                ", price=" + price +
                ", quantity=" + quantity +
                ", executedQuantity=" + executedQuantity +
                ", leavesQuantity=" + leavesQuantity +
                ", timeInForce=" + timeInForce +
                ", createDateTime=" + createDateTime +
                ", version=" + version +
                '}';
    }

    public String getSymbol() {
        return productId + "-" +currencyId;
    }
}
