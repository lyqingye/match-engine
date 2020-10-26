package com.trader.book.support.router;

import com.trader.book.OrderRouter;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;

import java.util.Collection;
import java.util.Collections;
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

    /**
     * 根据交易对进行映射
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿 or null 如果没有合适的订单
     */
    @Override
    public Collection<OrderBook> mapTo(String symbolId) {
        OrderBook book = bookCache.get(symbolId);
        if (book == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(book);
    }
}
