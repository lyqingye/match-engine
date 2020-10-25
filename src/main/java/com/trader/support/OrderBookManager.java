package com.trader.support;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yjt
 * @since 2020/9/19 下午4:30
 */
public class OrderBookManager {

    /**
     * 账本映射MAP
     * <p>
     * 在这里使用并发集合是为了做懒加载,并且用于后续支持多线程
     */
    private Map<String, OrderBook> bookMap = new ConcurrentHashMap<>(16);

    public OrderBookManager() {
    }

    /**
     * 根据订单获取该订单所在的账本
     *
     * @param order
     *         订单
     *
     * @return 账本
     */
    @Deprecated
    public OrderBook getBook(Order order) {
        return bookMap.computeIfAbsent(order.getSymbol(), key -> {
            OrderBook newBook = new OrderBook();
            newBook.setSymbolId(key);
            return newBook;
        });
    }

    /**
     * 根据订单获取该订单所在的账本
     *
     * @param identity
     *         唯一标识
     *
     * @return 账本
     */
    public OrderBook getBook(String identity) {
        return bookMap.computeIfAbsent(identity, key -> {
            OrderBook newBook = new OrderBook();
            newBook.setSymbolId(key);
            return newBook;
        });
    }

    /**
     * 该订单簿是否支持该订单
     *
     * @param order
     *         订单
     *
     * @return 是否支持
     */
    @Deprecated
    public boolean isSupport(Order order) {
        return bookMap.containsKey(order.getSymbol());
    }

    /**
     * 获取所有订单簿
     *
     * @return 所有订单簿
     */
    public Collection<OrderBook> listBooks() {
        if (bookMap.isEmpty()) {
            return Collections.emptyList();
        }
        return bookMap.values();
    }
}
