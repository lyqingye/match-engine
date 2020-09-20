package com.trader.support;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/9/19 下午4:30
 */
public class OrderBookManager {

    /**
     * 货币管理者
     */
    private CurrencyManager currencyMgr;

    /**
     * 产品管理器
     */
    private ProductManager productMgr;

    /**
     * 账本映射MAP
     */
    private Map<String,OrderBook> bookMap = new HashMap<>(16);

    /**
     * hide default constructor
     */
    private OrderBookManager () {}

    public OrderBookManager (CurrencyManager currencyMgr,ProductManager productMgr) {
        this.currencyMgr = Objects.requireNonNull(currencyMgr);
        this.productMgr = Objects.requireNonNull(productMgr);
    }

    /**
     * 根据订单获取该订单所在的账本
     *
     * @param order 订单
     * @return 账本
     */
    public OrderBook getBook (Order order) {
        OrderBook book = bookMap.get(order.getSymbol());
        if (book == null) {
            OrderBook newBook = new OrderBook();
            newBook.setProduct(productMgr.getProduct(order.getProductId()));
            newBook.setCurrency(currencyMgr.getCurrency(order.getCurrencyId()));
            book = newBook;
            bookMap.put(order.getSymbol(), newBook);
        }
        return book;
    }
}
