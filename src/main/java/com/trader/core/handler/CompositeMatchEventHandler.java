package com.trader.core.handler;

import com.trader.core.MatchHandler;
import com.trader.core.entity.Order;
import com.trader.core.matcher.TradeResult;

/**
 * @author yjt
 * @since 2020/10/27 上午11:00
 */
public class CompositeMatchEventHandler extends MatchEventHandlerRegistry implements MatchHandler {

    public CompositeMatchEventHandler(MatchHandler... handler) {
        for (MatchHandler h : handler) {
            super.regHandler(h);
        }
    }

    /**
     * 添加订单事件
     *
     * @param newOrder
     *         订单
     *
     * @throws Exception
     *         如果发生异常
     */
    @Override
    public void onAddOrder(Order newOrder) throws Exception {
        for (MatchHandler h : super.handlers()) {
            h.onAddOrder(newOrder);
        }
    }

    /**
     * 激活止盈止损订单事件
     *
     * @param stopOrder
     *         止盈止损订单
     *
     * @throws Exception
     *         如果发生异常
     */
    @Override
    public void onActiveStopOrder(Order stopOrder) throws Exception {
        for (MatchHandler h : super.handlers()) {
            h.onActiveStopOrder(stopOrder);
        }
    }

    /**
     * 订单移除事件
     * 警告: 当发生异常不会导致订单回滚, 为什么这么做呢,我来解释下
     * <p>
     * 因为撮合是单线程的, 订单移除可能是异步的, 这就会导致数据并发
     * 所以在调用取消订单的时候, 我只是对这个订单进行了标记
     * 当撮合引擎匹配到这个被标记为取消的订单后, 将不会被撮合 并且会调用
     * 当前事件, 所以这个事件不会涉及到持久化操作,所以不应该抛出异常
     *
     * @param removed
     *         已经被移除的事件
     */
    @Override
    public void onOrderCancel(Order removed) {
        for (MatchHandler h : super.handlers()) {
            h.onOrderCancel(removed);
        }
    }

    /**
     * 撮合订单事件
     *
     * @param order
     *         订单
     * @param opponentOrder
     *         对手订单
     * @param ts
     *         撮合结果
     *
     * @throws Exception
     *         如果发生异常
     */
    @Override
    public void onExecuteOrder(Order order, Order opponentOrder, TradeResult ts) throws Exception {
        for (MatchHandler h : super.handlers()) {
            h.onExecuteOrder(order, opponentOrder, ts);
        }
    }
}
