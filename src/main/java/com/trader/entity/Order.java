package com.trader.entity;

import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
     * 产品ID
     */
    private String productId;

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
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 已经执行的数量 （买入/卖出）的数量
     */
    private BigDecimal executedQuantity;

    /**
     * 剩余数量
     */
    private BigDecimal leavesQuantity;

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
    public Object clone() throws CloneNotSupportedException {
        Order order = new Order();
        order.id = id;
        order.productId = productId;
        order.price = price;
        order.createDateTime = createDateTime;
        order.executedQuantity = executedQuantity;
        order.leavesQuantity = leavesQuantity;
        order.quantity = quantity;
        order.side = side;
        order.type = type;
        order.timeInForce = timeInForce;
        order.version = version;
        return order;
    }

    public void restore (Order o) {
        Objects.requireNonNull(o);
        this.id = o.id;
        this.productId = o.productId;
        this.price = o.price;
        this.createDateTime = o.createDateTime;
        this.executedQuantity = o.executedQuantity;
        this.leavesQuantity = o.leavesQuantity;
        this.quantity = o.quantity;
        this.side = o.side;
        this.type = o.type;
        this.timeInForce = o.timeInForce;
        this.version = o.version;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", leavesQuantity=" + leavesQuantity +
                ", time=" + createDateTime.getTime() +
                '}';
    }
}
