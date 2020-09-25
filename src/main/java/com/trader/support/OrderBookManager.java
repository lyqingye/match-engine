package com.trader.support;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

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
     * @param symbolId
     *         交易对
     *
     * @return 账本
     */
    public OrderBook getBook(String symbolId) {
        return bookMap.computeIfAbsent(symbolId, key -> {
            OrderBook newBook = new OrderBook();
            newBook.setSymbolId(key);
            return newBook;
        });
    }
}
