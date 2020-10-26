package com.trader.book.support.processor;

import com.trader.MatchEngine;
import com.trader.Matcher;
import com.trader.book.OrderRouter;
import com.trader.book.Processor;
import com.trader.context.MatchingContext;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.event.MatchEventHandlerRegistry;
import com.trader.exception.TradeException;
import com.trader.market.MarketManager;
import com.trader.matcher.MatcherManager;
import com.trader.matcher.TradeResult;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 通用处理器,负责处理订单
 *
 * @author yjt
 * @since 2020/10/23 上午10:08
 */
public class GenericProcessor extends MatchEventHandlerRegistry implements Processor {

    /**
     * 默认的队列大小
     */
    private static final int DEFAULT_INPUT_QUEUE_SIZE = 64;

    /**
     * 输入队列
     */
    private DisruptorQueue<Order> inputQueue;

    /**
     * 订单簿管理器
     */
    private OrderRouter router;

    /**
     * 匹配器管理器
     */
    private MatcherManager matcherMgr;

    /**
     * 市场管理器
     */
    private MarketManager marketMgr;

    /**
     * 当前选中的订单簿
     */
    private OrderBook currentOrderBook;

    /**
     * 当前选中的匹配器
     */
    private Matcher currentMatcher;

    /**
     * 处理器名称
     */
    private String name;

    /**
     * 线程工厂
     */
    private ProcessorThreadFactory ptf;

    /**
     * hide default constructor
     */
    private GenericProcessor() {
    }

    public GenericProcessor(String name,
                            OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr,
                            int queueSize) {
        this.name = name;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = marketMgr;
        this.ptf = new ProcessorThreadFactory(this.name());

        // 队列创建
        inputQueue = DisruptorQueueFactory.createQueue(queueSize,
                                                       ptf,
                                                       new OrderProcessor());
    }

    public GenericProcessor(String name,
                            OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr) {
        this.name = name;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = marketMgr;
        this.ptf = new ProcessorThreadFactory(this.name());

        // 队列创建
        inputQueue = DisruptorQueueFactory.createQueue(DEFAULT_INPUT_QUEUE_SIZE,
                                                       ptf,
                                                       new OrderProcessor());
    }


    public String name() {
        return this.name;
    }

    public void renaming(String newName) {
        ptf.rename(this.name, newName);
        this.name = newName;
    }

    /**
     * 执行一个订单
     *
     * @param order
     *         订单
     */
    public void exec(Order order) {
        inputQueue.add(order);
    }

    /**
     * 真正的逻辑实现
     */
    class OrderProcessor extends AbstractDisruptorConsumer<Order> {

        /**
         * 进行数据处理
         *
         * @param order
         *         订单
         */
        @Override
        public void process(Order order) {
            OrderBook book = router.mapTo(order);

            // 如果为添加订单
            if (order.isAddCmd()) {
                book.addOrder(order);

                // 添加订单
                executeHandler(h -> {
                    try {
                        h.onAddOrder(order);
                    } catch (Exception e) {
                        throw new TradeException(e.getMessage());
                    }
                });
                matchOrder(book, order);
                return;
            }

            // 如果为取消订单
            if (order.isCancelCmd()) {
                book.removeOrder(order);
                executeOrderCancel(order);
            }
        }

        /**
         * 订单撮合
         *
         * @param book
         *         账本
         * @param order
         *         订单
         */
        private void matchOrder(OrderBook book, Order order) {
            //
            // 根据订单类型确定对手盘
            // 买入单: 则对手盘为卖盘
            // 卖出单: 则对手盘为买盘
            //
            Iterator<Order> opponentIt = null;
            if (order.isBuy()) {
                opponentIt = book.getAskOrders().iterator();
            } else {
                opponentIt = book.getBidOrders().iterator();
            }

            // 当前撮合的订单簿
            currentOrderBook = book;

            while (opponentIt.hasNext()) {
                Order best = opponentIt.next();

                // 查找订单匹配器
                Matcher matcher = matcherMgr.lookupMatcher(order, best);

                if (matcher == null) {
                    continue;
                }

                // 当前的匹配器
                currentMatcher = proxyMatcher(matcher);

                //
                // 订单结束状态补偿
                //
                if (currentMatcher.isFinished(order)) {
                    order.markFinished();
                    return;
                }

                if (currentMatcher.isFinished(best)) {
                    best.markFinished();

                    // 移除被标记的订单
                    opponentIt.remove();
                    if (order.isBuy()) {
                        opponentIt = book.getAskOrders().iterator();
                    } else {
                        opponentIt = book.getBidOrders().iterator();
                    }
                    continue;
                }

                // 执行撮合
                TradeResult ts = currentMatcher.doTrade(order, best);

                //
                // 事务
                //
                Order snap_order = order.snap();
                Order snap_best = best.snap();

                //
                // 处理订单撮合结果
                //
                executeHandler((handler) -> {
                    try {
                        //
                        // 执行事件调用链:
                        // 调用链的顶部必然是一个内存操作的 handler, 也就是必须先写入内存
                        // 可能也存在一个持久化的 handler, 所以需要在执行做事务处理
                        // 当 handler 发生异常, 我们将需要将内存数据进行回滚
                        handler.onExecuteOrder(order, best, ts);
                    } catch (Exception e) {
                        order.rollback(snap_order);
                        best.rollback(snap_best);
                        e.printStackTrace();
                        order.markCanceled();
                        best.markCanceled();
                        return;
                    }
                });

                // 移除已经结束的订单
                if (currentMatcher.isFinished(best)) {

                    // 标记订单已结束
                    best.markFinished();

                    // 直接使用
                    opponentIt.remove();

                    // 推送事件
                    executeOrderCancel(best);

                    if (order.isBuy()) {
                        opponentIt = book.getAskOrders().iterator();
                    } else {
                        opponentIt = book.getBidOrders().iterator();
                    }
                }

                // 撮合结束
                if (currentMatcher.isFinished(order)) {
                    //
                    // 标记已经结束的订单并且结束撮合
                    //
                    order.markFinished();
                    return;
                }
            }
        }

        private UnsafeMatchContext CACHED_MATCH_CONTEXT;

        private Matcher proxyMatcher(Matcher target) {
            if (CACHED_MATCH_CONTEXT == null) {
                CACHED_MATCH_CONTEXT = new UnsafeMatchContext();
            }
            CACHED_MATCH_CONTEXT.clearAttrs();

            return new Matcher() {
                @Override
                public boolean isSupport(Order order, Order opponentOrder) {
                    return target.isSupport(order, opponentOrder);
                }

                @Override
                public TradeResult doTrade(Order order, Order opponentOrder) {
                    return target.doTrade(order, opponentOrder);
                }

                @Override
                public boolean isFinished(Order order) {
                    return target.isFinished(order);
                }

                @Override
                public MatchingContext ctx() {
                    return CACHED_MATCH_CONTEXT;
                }
            };
        }

        @SuppressWarnings("unchecked")
        class UnsafeMatchContext implements MatchingContext {
            private Map<String, Object> attr = new HashMap<>(16);

            @Override
            public MarketManager getMarketMgr() {
                return marketMgr;
            }

            @Override
            public MatchEngine getEngine() {
                return null;
            }

            @Override
            public OrderBook getOrderBook() {
                return currentOrderBook;
            }

            @Override
            public Matcher getMatcher() {
                return currentMatcher;
            }

            @Override
            public <T> T getAttribute(String key) {
                return (T) attr.get(key);
            }

            @Override
            public void setAttribute(String key, Object value) {
                attr.put(key, value);
            }

            public void clearAttrs() {
                attr.clear();
            }
        }
    }
}
