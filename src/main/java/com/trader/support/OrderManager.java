package com.trader.support;

import com.trader.entity.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/9/19 下午4:30
 */
public class OrderManager {

    /**
     * 订单映射MAP
     */
    private Map<String, Order> orderMap = new HashMap<>(64);

    /**
     * 添加一个订单
     *
     * @param order 订单对象
     */
    public void addOrder (Order order) {
        this.orderCheck(Objects.requireNonNull(order));
        this.orderMap.put(order.getId(),order);
    }

    /**
     * 移除一个订单
     *
     * @param order 订单对象
     */
    public void removeOrder (Order order) {
        Objects.requireNonNull(order);
        this.orderMap.remove(order.getId());
    }

    /**
     * 根据订单ID查询订单对象
     *
     * @param orderId 订单ID
     * @return 订单对象
     */
    public Order getOrder (String orderId) {
        return this.orderMap.get(orderId);
    }

    /**
     * 订单检查
     *
     * @param order 订单
     */
    private void orderCheck (Order order) {
        switch (order.getType()) {
            case LIMIT: {
                break;
            }
            case STOP: {
                break;
            }
            case MARKET: {
                break;
            }
            default: {
                throw new IllegalArgumentException("非法的订单类型");
            }
        }
    }
}
