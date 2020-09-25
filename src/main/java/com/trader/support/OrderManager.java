package com.trader.support;

import com.trader.entity.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yjt
 * @since 2020/9/19 下午4:30
 */
public class OrderManager {

    /**
     * 订单映射MAP
     */
    private Map<String, Order> orderMap = new ConcurrentHashMap<>(4096);

    /**
     * 添加一个订单
     *
     * @param order
     *         订单对象
     */
    public void addOrder(Order order) {
        this.orderCheck(Objects.requireNonNull(order));
        this.orderMap.put(order.getId(), order);
    }

    /**
     * 移除一个订单
     *
     * @param order
     *         订单对象
     */
    public void removeOrder(Order order) {
        Objects.requireNonNull(order);
        this.orderMap.remove(order.getId());
    }

    /**
     * 根据订单ID查询订单对象
     *
     * @param orderId
     *         订单ID
     *
     * @return 订单对象
     */
    public Order getOrder(String orderId) {
        return this.orderMap.get(orderId);
    }

    /**
     * 订单检查
     *
     * @param order
     *         订单
     */
    private void orderCheck(Order order) {
        this.genericCheck(order);
        switch (order.getType()) {
            case LIMIT: {
                this.checkLimitOrder(order);
                break;
            }
            case STOP: {
                this.checkStopOrder(order);
                break;
            }
            case MARKET: {
                this.checkMarketOrder(order);
                break;
            }
            default: {
                throw new IllegalArgumentException("非法的订单类型");
            }
        }
    }

    /**
     * 检查限价订单的合法性
     *
     * @param order
     *         订单
     */
    private void checkLimitOrder(Order order) {
        if (order.isBuy()) {
            if (order.getPriceLowerBound().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("[限价][买入]订单不允许存在下限");
            }

            // 总金额 = 数量 * 单价
            BigDecimal totalAmount = order.getQuantity().multiply(order.getPrice());
            if (totalAmount.compareTo(order.getTotalAmount()) != 0) {
                throw new IllegalArgumentException("[限价][买入]订单总金额 != 数量 x 单价");
            }

            if (order.getTotalAmount().compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("[限价][买入]非法总金额");
            }

            if (order.getLeavesQuantity().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("[限价][买入]订单待执行数量 != 0");
            }
        }

        if (order.getTriggerPrice().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("[限价][买入/卖出]订单不允许设置触发价格");
        }

        if (order.isSell()) {
            if (order.getPriceUpperBound().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("[限价][卖出]订单不允许存在上界");
            }

            if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("[限价][卖出]订单总数量 != 0");
            }

            if (order.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("[限价][卖出]订单没有设置单价");
            }
        }
    }

    /**
     * 检查市价订单的合法性
     *
     * @param order
     */
    private void checkMarketOrder(Order order) {
        if (order.getPriceLowerBound().compareTo(BigDecimal.ZERO) != 0 ||
                order.getPriceUpperBound().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("[市价][买入/卖出]订单不允许存在上界下限");
        }
        if (order.getTriggerPrice().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("[市价][买入/卖出]订单,触发价格必须等于0");
        }
    }

    /**
     * 检查止盈止损订单的合法性
     *
     * @param order
     *         订单
     */
    private void checkStopOrder(Order order) {
        if (order.getTriggerPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("[止盈止损][买入/卖出]订单没有设置触发价格");
        }
    }

    /**
     * 常规检查
     *
     * @param order 订单
     */
    private void genericCheck (Order order) {
        if (order == null) {
            throw new IllegalArgumentException("订单不能为空");
        }

        if (order.getSymbol() == null || order.getSymbol().isEmpty()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
    }
}
