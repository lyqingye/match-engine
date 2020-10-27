package com.trader.core.support.router;

import com.trader.core.OrderRouter;
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
    public OrderBook routeTo(Order order) {
        return bookCache.computeIfAbsent(order.getSymbol(), k -> {
            final OrderBook newBook = new OrderBook();
            newBook.setSymbolId(k);
            return newBook;
        });
    }

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要触发止盈止损订单的订单簿
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿集合
     */
    @Override
    public Collection<OrderBook> routeToNeedToActiveBook(String symbolId) {
        OrderBook book = bookCache.get(symbolId);
        if (book == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(book);
    }

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要更新最新成交价的订单簿
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿集合
     */
    @Override
    public Collection<OrderBook> routeToNeedToUpdatePriceBook(String symbolId) {
        OrderBook book = bookCache.get(symbolId);
        if (book == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(book);
    }

    /**
     * 给定一个交易对, 返回一个订单簿, 用于第三方调用者查询市场价
     *
     * @param symbolId
     *         交易对
     *
     * @return 订单簿
     */
    @Override
    public OrderBook routeToBookForQueryPrice(String symbolId) {
        return bookCache.get(symbolId);
    }

    /**
     * 给定一个订单, 返回该订单实际撮合获取市价的订单簿
     * 用于撮合过程中, 如果该订单为市价单, 则从该订单簿获取最新成交价
     *
     * @param order
     *         订单
     *
     * @return 订单簿
     */
    @Override
    public OrderBook routeToBookForQueryPrice(Order order) {
        return bookCache.get(order.getSymbol());
    }

    /**
     * 给定一个订单, 返回一个订单簿, 当该订单引起盘口变化时
     * 需要根据该订单获取需要发送的盘口
     *
     * @param order
     *         订单
     *
     * @return 订单簿
     */
    @Override
    public OrderBook routeToBookForSendDepthChart(Order order) {
        return bookCache.get(order.getSymbol());
    }
}
