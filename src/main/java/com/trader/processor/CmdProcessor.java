package com.trader.processor;

import com.trader.cmd.*;
import com.trader.core.Matcher;
import com.trader.core.context.MatchingContext;
import com.trader.core.context.ThreadLocalMatchingContext;
import com.trader.core.entity.Order;
import com.trader.core.entity.OrderBook;
import com.trader.core.exception.MatchExceptionHandler;
import com.trader.core.matcher.MatcherManager;
import com.trader.core.matcher.MatchResult;
import com.trader.market.MarketManager;
import com.trader.utils.ThreadLocalUtils;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import java.util.Iterator;

/**
 * 命令处理器
 *
 * @author yjt
 */
public class CmdProcessor extends AbstractDisruptorConsumer<ICmd> {
    // 处理器名称
    private String name;
    // 命令队列
    private DisruptorQueue<ICmd> cmdQueue;
    // 匹配器管理器
    private MatcherManager matcherMgr;
    // 市场管理器
    private MarketManager marketMgr;
    // 当前选中的订单簿
    private OrderBook curMatchBook;
    // 当前选中的匹配器
    private Matcher curMatcher;
    // 异常处理器
    private MatchExceptionHandler matchExceptionHandler;

    /**
     * 提交命令
     * @param cmd 命令
     */
    public void submitCmd (ICmd cmd) {
        cmdQueue.add(cmd);
    }

    @Override
    public void process(ICmd cmd) {
        if (cmd instanceof OrderAddCmd) {
            doAdd((OrderAddCmd) cmd);
            return;
        }
        if (cmd instanceof OrderCancelCmd) {
            doCancel((OrderCancelCmd) cmd);
            return;
        }
        if (cmd instanceof OrderActiveCmd) {
            doActive((OrderActiveCmd) cmd);
            return;
        }
        if (cmd instanceof PriceChangeCmd) {
            doPriceChange((PriceChangeCmd) cmd);
        }
    }

    private void doAdd (OrderAddCmd cmd) {
    }

    private void doMatch(OrderAddCmd cmd) {
        OrderBook book = new OrderBook();
        Order order = cmd.getOrder();
        order.markMatching();
        Iterator<Order> opponentIt = null;
        if (order.isBuy()) {
            opponentIt = book.askLimitOrderIt();
        } else {
            opponentIt = book.bidLimitOrderIt();
        }
        // 当前撮合的订单簿
        curMatchBook = book;
        // 构建上下文
        buildContext();
        while (opponentIt.hasNext()) {
            Order best = opponentIt.next();
            best.markMatching();
            // 查找订单匹配器
            Matcher matcher = matcherMgr.lookupMatcher(order, best,cachedMatchingContext);

            if (matcher == null) {
                // 这里有可能是因为买单没有足够的余额进行成交, 所以是 continue 而不是 return
                best.unMarkMatching();
                continue;
            }
            setMatcherOnContext();
            curMatcher = matcher;
            // 订单结束状态补偿
            if (curMatcher.isFinished(order)) {
                order.markFinished();
                order.unMarkMatching();
                best.unMarkMatching();
                return;
            }

            if (curMatcher.isFinished(best)) {
                // 移除被标记的订单
                opponentIt.remove();
                if (order.isBuy()) {
                    opponentIt = book.askLimitOrderIt();
                } else {
                    opponentIt = book.bidLimitOrderIt();
                }
                best.unMarkMatching();
                continue;
            }
            // 执行撮合
            MatchResult ts = curMatcher.doTrade(order, best,cachedMatchingContext);
            // 事务
            Order snap_order = order.snap();
            Order snap_best = best.snap();
//            // 处理订单撮合结果
//            executeHandler((handler) -> {
//                try {
//                    handler.onExecuteOrder(order, best, ts);
//                } catch (Exception e) {
//                    order.rollback(snap_order);
//                    best.rollback(snap_best);
//                    order.markCanceled();
//                    best.markCanceled();
//                    matchExceptionHandler.handler(Thread.currentThread().getName(),
//                            name(), e,
//                            String.format("TradeException:\n curOrder: %s\n opponentOrder: %s\n ts: %s\n", order, best, ts.toString()));
//                }
//            });

            // 移除已经结束的订单
            if (curMatcher.isFinished(best)) {
                opponentIt.remove();
                // 推送事件
//                executeOrderCancel(best);
                if (order.isBuy()) {
                    opponentIt = book.getAskLimitOrders().iterator();
                } else {
                    opponentIt = book.getBidLimitOrders().iterator();
                }
            }
            // 撮合结束
            if (curMatcher.isFinished(order)) {
                order.markFinished();
            }
            best.unMarkMatching();
        }
        order.unMarkMatching();
    }

    private void doCancel(OrderCancelCmd cmd) {
    }

    private void doActive(OrderActiveCmd cmd) {
    }

    private void doPriceChange(PriceChangeCmd cmd) {}

    private MatchingContext cachedMatchingContext;

    /**
     * 构建上下文
     */
    private void buildContext() {
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_ORDER_BOOK, curMatchBook);
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MARKET_MANAGER, marketMgr);

        if (cachedMatchingContext == null) {
            cachedMatchingContext = new ThreadLocalMatchingContext();
            ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_CONTEXT, cachedMatchingContext);
        }
    }

    /**
     * 设置 matcher 到上下文
     */
    private void setMatcherOnContext() {
//        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MATCHER, currentMatcher);
    }
}
