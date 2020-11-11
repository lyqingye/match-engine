package com.trader.core.support.router.bot;

import com.trader.core.OrderRouter;
import com.trader.core.def.Category;
import com.trader.core.entity.Order;
import com.trader.core.entity.OrderBook;

import java.util.*;

public class KlineBotOrderRouter implements OrderRouter {

    /**
     * 用户订单映射
     * symbol -> book
     */
    private Map<String, OrderBook> userBookCache = new HashMap<>(16);

    /**
     * 机器人订单映射
     * symbol -> book
     */
    private Map<String, OrderBook> botBookCache = new HashMap<>(16);

    /**
     * 路由的名字
     *
     * @return 路由的名字
     */
    @Override
    public String name() {
        return KlineBotOrderRouter.class.getSimpleName();
    }

    /**
     * 路由的描述
     *
     * @return 路由的描述
     */
    @Override
    public String desc() {
        return "K线机器人订单路由";
    }

    /**
     * 根据所给的订单信息, 映射到指定的订单簿
     *
     * @param order 订单
     * @return 订单所在盘, 不能为null
     */
    @Override
    public OrderBook routeTo(Order order) {

        // 用户订单和用户订单进行撮合
        if (order.getCategory() == Category.USER) {
            return userBookCache.computeIfAbsent(order.getSymbol(), k -> {
                final OrderBook newBook = new OrderBook();
                newBook.setSymbolId(k);
                return newBook;
            });
        }

        // 机器人订单和机器人订单进行撮合
        return botBookCache.computeIfAbsent(order.getSymbol(), k -> {
            final OrderBook newBook = new OrderBook();
            newBook.setSymbolId(k);
            return newBook;
        });
    }

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要触发止盈止损订单的订单簿
     *
     * @param symbolId 交易对
     * @return 订单簿集合
     */
    @Override
    public Collection<OrderBook> routeToNeedToActiveBook(String symbolId) {
        // 只激活用户订单的止盈止损订单
        OrderBook book = userBookCache.get(symbolId);
        if (book == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(book);
    }

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要更新最新成交价的订单簿
     *
     * @param symbolId 交易对
     * @return 订单簿集合
     */
    @Override
    public Collection<OrderBook> routeToNeedToUpdatePriceBook(String symbolId) {
        // 用户订单盘口和机器人订单盘口都更新市场价格
        OrderBook user = userBookCache.get(symbolId);
        OrderBook bot = botBookCache.get(symbolId);
        Collection<OrderBook> books = new ArrayList<>(2);
        if (user != null) {
            books.add(user);
        }
        if (bot != null) {
            books.add(bot);
        }
        return books;
    }

    /**
     * 给定一个交易对, 当市场价格变动的时候, 根据交易对
     * 获取需要更新最新成交价的订单簿
     *
     * @param order 订单
     * @return 订单簿集合
     */
    @Override
    public Collection<OrderBook> routeToNeedToUpdatePriceBook(Order order) {
        return routeToNeedToUpdatePriceBook(order.getSymbol());
    }

    /**
     * 给定一个交易对, 返回一个订单簿, 用于第三方调用者查询市场价
     *
     * @param symbolId 交易对
     * @return 订单簿
     */
    @Override
    public OrderBook routeToBookForQueryPrice(String symbolId) {
        // 所有查询订单价格都从用户盘口查询
        return userBookCache.get(symbolId);
    }

    /**
     * 给定一个订单, 返回该订单实际撮合获取市价的订单簿
     * 用于撮合过程中, 如果该订单为市价单, 则从该订单簿获取最新成交价
     *
     * @param order 订单
     * @return 订单簿
     */
    @Override
    public OrderBook routeToBookForQueryPrice(Order order) {
        return userBookCache.get(order.getSymbol());
    }

    /**
     * 给定一个订单, 返回一个订单簿, 当该订单引起盘口变化时
     * 需要根据该订单获取需要发送的盘口
     *
     * @param order 订单
     * @return 订单簿 or null 如果不需要推送盘口
     */
    @Override
    public OrderBook routeToBookForSendDepthChart(Order order) {
        // 如果为用户订单则发送盘口
        if (order.getCategory() == Category.USER) {
            return userBookCache.get(order.getSymbol());
        }

        // 机器人不需要发送盘口
        return null;
    }

    /**
     * 对于本次撮合是否推送K线
     *
     * @param order         当前订单
     * @param opponentOrder 对手订单
     * @return 是否推送
     */
    @Override
    public boolean isPublishKline(Order order, Order opponentOrder) {
        return true;
    }
}
