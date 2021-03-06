package com.trader.core.support.processor;

import com.trader.core.Matcher;
import com.trader.core.OrderRouter;
import com.trader.core.Processor;
import com.trader.core.context.MatchingContext;
import com.trader.core.context.ThreadLocalMatchingContext;
import com.trader.core.def.ActivateStatus;
import com.trader.core.def.Cmd;
import com.trader.core.entity.Order;
import com.trader.core.entity.OrderBook;
import com.trader.core.handler.MatchEventHandlerRegistry;
import com.trader.core.exception.MatchExceptionHandler;
import com.trader.core.matcher.MatcherManager;
import com.trader.core.matcher.TradeResult;
import com.trader.market.MarketManager;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.utils.ThreadLocalUtils;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;

import java.util.Collection;
import java.util.Iterator;
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
     * 异常处理器
     */
    private MatchExceptionHandler matchExceptionHandler;

    /**
     * hide default constructor
     */
    private GenericProcessor() {
    }

    public GenericProcessor(String name,
                            OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr,
                            MatchExceptionHandler matchExceptionHandler,
                            int queueSize) {
        this.name = name;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = Objects.requireNonNull(marketMgr);
        this.matchExceptionHandler = matchExceptionHandler;
        this.ptf = new ProcessorThreadFactory(this.name());

        // 队列创建
        inputQueue = DisruptorQueueFactory.createSingleQueue(queueSize,
                ptf,
                new OrderProcessor(),
                matchExceptionHandler.toDisruptorHandler());
    }

    public GenericProcessor(String name,
                            OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr) {
        this.name = name;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = Objects.requireNonNull(marketMgr);
        this.ptf = new ProcessorThreadFactory(this.name());

        // 队列创建
        inputQueue = DisruptorQueueFactory.createSingleQueue(DEFAULT_INPUT_QUEUE_SIZE,
                ptf,
                new OrderProcessor(),
                matchExceptionHandler.toDisruptorHandler());
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void renaming(String newName) {
        ptf.rename(this.name, newName);
        this.name = newName;
    }

    /**
     * 撮合核心停止工作并且剩余数据处理
     */
    @Override
    public void shutdownAndWait() {
        inputQueue.shutdown();
    }

    /**
     * 执行一个订单
     *
     * @param order 订单
     */
    @Override
    public void exec(Order order) {
        inputQueue.add(order);
    }

    /**
     * 市价价格变动
     */
    @Override
    public void execPriceChange(PriceChangeMessage msg) {
        Collection<OrderBook> books = router.routeToNeedToActiveBook(msg.getSymbol());
        if (books.isEmpty()) {
            return;
        }
        for (OrderBook book : books) {
            if (!book.getBuyStopOrders().isEmpty()) {
                Iterator<Order> bidIt = book.getBuyStopOrders().iterator();
                while (bidIt.hasNext()) {
                    Order bid = bidIt.next();
                    if (bid.isNotActivated()) {
                        if (bid.getTriggerPrice().compareTo(msg.getPrice()) >= 0) {
                            System.out.println(String.format("[MatchProcessor]: active stop order," +
                                            " orderId: [%s] side: [%s]" +
                                            "triggerPrice: [%s] latestPrice: [%s]",
                                    bid.getId(),
                                    bid.getSide().name(),
                                    bid.getTriggerPrice().toPlainString(),
                                    msg.getPrice().toPlainString()));
                            bid.setCmd(Cmd.ACTIVE_ORDER);
                            bid.setActivated(ActivateStatus.ACTIVATING);
                            exec(bid);
                        } else {
                            break;
                        }
                    } else if (bid.isActivated()) {
                        bidIt.remove();
                    }
                }
                continue;
            }

            if (!book.getSellStopOrders().isEmpty()) {
                Iterator<Order> askIt = book.getSellStopOrders().iterator();
                while (askIt.hasNext()) {
                    Order ask = askIt.next();

                    if (ask.isNotActivated()) {
                        if (ask.getTriggerPrice().compareTo(msg.getPrice()) <= 0) {
                            System.out.println(String.format("[MatchProcessor]: active stop order, " +
                                            "orderId: [%s] side: [%s]" +
                                            "triggerPrice: [%s] latestPrice: [%s]",
                                    ask.getId(),
                                    ask.getSide().name(),
                                    ask.getTriggerPrice().toPlainString(),
                                    msg.getPrice().toPlainString()));
                            ask.setCmd(Cmd.ACTIVE_ORDER);
                            ask.setActivated(ActivateStatus.ACTIVATING);
                            exec(ask);
                        }else {
                            break;
                        }
                    } else if (ask.isActivated()) {
                        askIt.remove();
                    }
                }
            }
        }
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
            OrderBook book = router.routeTo(order);

            // 如果为添加订单
            if (order.isAddCmd()) {
                book.addOrder(order);

                // 添加订单
                executeHandler(h -> {
                    try {
                        h.onAddOrder(order);
                    } catch (Exception e) {
                        matchExceptionHandler.handler(Thread.currentThread().getName(),
                                name(), e, String.format("AddOrder: curOrderId: %s", order.getId()));
                    }
                });
                matchOrder(book, order);
                return;
            }

            // 如果为取消订单
            if (order.isCancelCmd()) {
                book.removeOrder(order);
                executeOrderCancel(order);
                return;
            }

            // 如果为激活止盈止损命令
            if (order.isActiveCmd()) {
                // 账本激活止盈止损订单
                // 也就是将止盈利止损订单放入撮合买卖盘
                book.activeStopOrder(order);

                // 添加订单
                executeHandler(h -> {
                    try {
                        h.onActiveStopOrder(order);
                    } catch (Exception e) {
                        order.setActivated(ActivateStatus.NO_ACTIVATED);
                        // 如果激活订单失败则将该订单从买卖盘中移除
                        book.removeOrder(order);
                        matchExceptionHandler.handler(Thread.currentThread().getName(),
                                name(), e, String.format("ActiveStopOrder: curOrderId: %s", order.getId()));
                    }
                });

                // 标记订单为已经激活, 止盈止损的订单是另外一条线程（监听市价变动的处理线程）进行检测激活并放入订单事件队列的
                // 所以在这里我们只是做一个标记处理, 依旧是由监听市价变动的线程进行处理
                order.setActivated(ActivateStatus.ACTIVATED);
                matchOrder(book, order);
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
            order.markMatching();

            // 根据订单类型确定对手盘
            // 买入单: 则对手盘为卖盘
            // 卖出单: 则对手盘为买盘
            Iterator<Order> opponentIt = null;
            if (order.isBuy()) {
                opponentIt = book.getAskOrders().iterator();
            } else {
                opponentIt = book.getBidOrders().iterator();
            }

            // 当前撮合的订单簿
            currentOrderBook = book;

            // 构建上下文
            buildContext();

            while (opponentIt.hasNext()) {
                Order best = opponentIt.next();
                best.markMatching();

                // 查找订单匹配器
                Matcher matcher = matcherMgr.lookupMatcher(order, best);

                if (matcher == null) {
                    // 这里有可能是因为买单没有足够的余额进行成交, 所以是 continue 而不是 return
                    best.unMarkMatching();
                    continue;
                }

                // 设置匹配器到上下文中
                setMatcherOnContext();

                // 当前的匹配器
                currentMatcher = matcher;

                // 订单结束状态补偿
                if (currentMatcher.isFinished(order)) {
                    order.markFinished();
                    order.unMarkMatching();
                    best.unMarkMatching();
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
                    best.unMarkMatching();
                    continue;
                }

                // 执行撮合
                TradeResult ts = currentMatcher.doTrade(order, best);

                // 事务
                Order snap_order = order.snap();
                Order snap_best = best.snap();

                // 处理订单撮合结果
                executeHandler((handler) -> {
                    try {
                        // 执行事件调用链:
                        // 调用链的顶部必然是一个内存操作的 handler, 也就是必须先写入内存
                        // 可能也存在一个持久化的 handler, 所以需要在执行做事务处理
                        // 当 handler 发生异常, 我们将需要将内存数据进行回滚
                        handler.onExecuteOrder(order, best, ts);
                    } catch (Exception e) {
                        order.rollback(snap_order);
                        best.rollback(snap_best);
                        order.markCanceled();
                        best.markCanceled();
                        matchExceptionHandler.handler(Thread.currentThread().getName(),
                                name(), e,
                                String.format("TradeException:\n curOrder: %s\n opponentOrder: %s\n ts: %s\n", order, best, ts.toString()));
                    }
                });

                // 移除已经结束的订单
                if (currentMatcher.isFinished(best)) {

                    // 标记订单已结束
                    best.markFinished();

                    // 直接移除
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
                }
                best.unMarkMatching();
            }
            order.unMarkMatching();
        }

        private MatchingContext cachedMatchingContext;

        /**
         * 构建上下文
         */
        private void buildContext() {
            ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_ORDER_BOOK, currentOrderBook);
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
            ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MATCHER, currentMatcher);
        }
    }
}
