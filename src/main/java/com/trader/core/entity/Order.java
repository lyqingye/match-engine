package com.trader.core.entity;

import com.trader.core.def.*;
import com.trader.utils.SymbolUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import static com.trader.utils.ArithmeticUtils.*;

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
     * 指令
     */
    private Cmd cmd = Cmd.ADD_ORDER;

    /**
     * 订单种类
     * + 用户下单
     * + 机器人下单
     */
    private Category category = Category.USER;

    /**
     * 订单ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 计价货币ID
     */
    private String currencyId;

    /**
     * 币种ID
     */
    private String coinId;

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
    private DiffPriceStrategy diffPriceStrategy = DiffPriceStrategy.DRIVER;

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
     * 止盈止损激活标志
     */
    private volatile ActivateStatus activated = ActivateStatus.NO_ACTIVATED;

    /**
     * 是否已经被标记为订单已经结束
     */
    private volatile boolean finished = false;

    /**
     * 是否已经被标记为订单已经取消
     */
    private volatile boolean canceled = false;

    /**
     * 是否正在匹配
     */
    private volatile boolean matching = false;

    /**
     * 版本 (预留)
     */
    private long version;


    public boolean isBuy() {
        return OrderSide.BUY.equals(side);
    }

    public boolean isSell() {
        return OrderSide.SELL.equals(side);
    }

    public boolean isFOK() {
        return OrderTimeInForce.FOK.equals(this.timeInForce);
    }

    public boolean isAON() {
        return OrderTimeInForce.AON.equals(this.timeInForce);
    }

    public boolean isLimitOrder() {
        return OrderType.LIMIT.equals(this.type);
    }

    public boolean isMarketOrder() {
        return OrderType.MARKET.equals(this.type);
    }

    public boolean isStopOrder() {
        return OrderType.STOP.equals(this.type);
    }

    public boolean isBuyMarketOrder() {
        return this.isBuy() && this.isMarketOrder();
    }

    public boolean isSellMarketOrder() {
        return this.isSell() && this.isMarketOrder();
    }

    /**
     * @return 当前订单的最高买入价或最低卖出价
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
            return add(price,mul(price,this.priceUpperBound));
        } else {
            return sub(price,mul(price,this.priceLowerBound));
        }
    }

    public void decLeavesQuality(BigDecimal q) {
        this.leavesQuantity = sub(leavesQuantity,q);
    }

    public void incExecutedQuality(BigDecimal q) {
        this.executedQuantity = add(executedQuantity,q);
    }

    public void decLeavesAmount(BigDecimal q) {
        this.leavesAmount = sub(leavesAmount,q);
    }

    public void incExecutedAmount(BigDecimal q) {
        this.executedAmount = add(executedAmount,q);
    }

    public boolean isAddCmd() {
        return Cmd.ADD_ORDER.equals(this.cmd);
    }

    public boolean isCancelCmd() {
        return Cmd.CANCEL_ORDER.equals(this.cmd);
    }

    public boolean isActiveCmd() {
        return Cmd.ACTIVE_ORDER.equals(this.cmd);
    }

    public boolean isActivated() {
        return activated.equals(ActivateStatus.ACTIVATED);
    }

    public boolean isNotActivated() {
        return activated.equals(ActivateStatus.NO_ACTIVATED);
    }

    public boolean isActivating() {
        return activated.equals(ActivateStatus.ACTIVATING);
    }

    public void markFinished() {
        this.finished = true;
    }

    public void markCanceled() {
        this.canceled = true;
    }

    public boolean isMatching() {
        return this.matching;
    }

    public void markMatching() {
        if (isMatching()) {
            System.err.println("[MatchEngine]: 订单: " + id + " 没有正确释放撮合锁!");
        }
        this.matching = true;
    }

    public void unMarkMatching() {
        this.matching = false;
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
                Objects.equals(version, order.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public Order clone() {
        Order order = new Order();
        order.cmd = cmd;
        order.id = id;
        order.uid = uid;
        order.coinId = coinId;
        order.currencyId = currencyId;
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

        order.triggerPrice = triggerPrice;
        order.diffPriceStrategy = diffPriceStrategy;


        order.side = side;
        order.type = type;
        order.timeInForce = timeInForce;
        order.version = version;

        order.activated = activated;
        order.finished = finished;
        order.canceled = canceled;
        order.matching = matching;
        return order;
    }

    public Order snap() {
        return this.clone();
    }

    public void rollback(Order o) {
        Objects.requireNonNull(o);
        this.cmd = o.cmd;
        this.id = o.id;
        this.uid = o.uid;
        this.coinId = o.coinId;
        this.currencyId = o.currencyId;
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

        this.triggerPrice = o.triggerPrice;
        this.diffPriceStrategy = o.diffPriceStrategy;

        this.side = o.side;
        this.type = o.type;
        this.timeInForce = o.timeInForce;
        this.version = o.version;

        this.activated = o.activated;
        this.finished = o.finished;
        this.canceled = o.canceled;
        this.matching = o.matching;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", coinId='" + coinId + '\'' +
                ", type=" + type +
                ", side=" + side +
                ", differencePriceStrategy=" + diffPriceStrategy +
                ", price=" + price +
                ", priceUpperBound=" + priceUpperBound +
                ", priceLowerBound=" + priceLowerBound +
                ", triggerPrice=" + triggerPrice +
                ", quantity=" + quantity +
                ", executedQuantity=" + executedQuantity +
                ", leavesQuantity=" + leavesQuantity +
                ", totalAmount=" + totalAmount +
                ", executedAmount=" + executedAmount +
                ", leavesAmount=" + leavesAmount +
                ", timeInForce=" + timeInForce +
                ", createDateTime=" + createDateTime +
                ", activated=" + activated +
                ", finished=" + finished +
                ", canceled=" + canceled +
                ", version=" + version +
                '}';
    }

    public String getSymbol() {
        return SymbolUtils.makeSymbol(coinId, currencyId);
    }

}
