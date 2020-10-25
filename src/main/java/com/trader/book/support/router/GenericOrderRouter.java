package com.trader.book.support.router;

import com.trader.book.OrderRouter;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.support.OrderBookManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 通用的订单路由
 * 根据交易对进行分区
 *
 * @author yjt
 * @since 2020/10/23 上午8:26
 */
public class GenericOrderRouter implements OrderRouter {

    /**
     * symbol -> book
     */
    private Map<String,OrderBook> bookCache = new HashMap<>(16);

    @Override
    public String name() {
        return GenericOrderRouter.class.getSimpleName();
    }

    @Override
    public String desc() {
        return "通用的订单路由, 根据交易对区分订单簿";
    }

    @Override
    public OrderBook mapTo(Order order) {
        return bookCache.computeIfAbsent(order.getSymbol(), k -> {
            final OrderBook newBook = new OrderBook();
            newBook.setSymbolId(k);
            return newBook;
        });
    }
}
