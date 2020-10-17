package com.trader;

import com.trader.context.ThreadLocalMatchingContext;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.exception.TradeException;
import com.trader.market.MarketEventHandler;
import com.trader.market.MarketManager;
import com.trader.matcher.TradeResult;
import com.trader.support.OrderBookManager;
import com.trader.support.OrderManager;
import com.trader.utils.ThreadLocalUtils;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * TODO:
 * 1. 撮合引擎需要实现 LiftCycle 接口
 * 2. 撮合引擎需要实现事物功能,避免内存撮合刷库导致的双写数据一致性问题
 * 3. 委托账本得独立
 * 4. 产品和货币得独立
 *
 * @author yjt
 * @since 2020/9/1 上午10:56
 */
@Log
public class MatchEngine {

    /**
     * 标志撮合引擎是否正在进行撮合
     */
    private volatile boolean isMatching = false;

    /**
     * 是否开启日志
     */
    private volatile boolean isEnableLog = false;

    /**
     * 处理器列表
     */
    private List<MatchHandler> handlers = new ArrayList<>(16);

    /**
     * 撮合匹配器
     */
    private List<Matcher> matchers = new ArrayList<>(16);

    /**
     * 账本管理器
     */
    @Getter
    private OrderBookManager bookMgr;

    /**
     * 订单管理器
     */
    @Getter
    private OrderManager orderMgr;

    /**
     * 市场管理器
     */
    @Getter
    private MarketManager marketMgr;

    /**
     * 下单队列
     */
    private DisruptorQueue<Order> addOrderQueue;

    /**
     * 激活止盈止损订单队列
     */
    private DisruptorQueue<Order> activeStopOrderQueue;

    public MatchEngine() {
        this.orderMgr = new OrderManager();
        this.bookMgr = new OrderBookManager();
        this.marketMgr = new MarketManager(bookMgr);

        // 将行情管理器的撮合监听事件添加进撮合引擎
        this.addHandler(this.marketMgr.getMatchHandler());

        //
        // 设置市价变动事件
        // 止盈止损订单需要监听市场价格变动的事件
        // 为了提高吞吐量, 需要引入队列, 当市场止盈止损订单被触发的时候将需要下单的订单
        //
        final MatchEngine that = this;
        marketMgr.addHandler(new MarketEventHandler() {
            @Override
            public void onMarketPriceChange(String symbol,
                                            BigDecimal latestPrice, boolean third) {
                OrderBook book = that.bookMgr.getBook(symbol);

                try {
                    Iterator<Order> bidIt = book.getBuyStopOrders().iterator();
                    while (bidIt.hasNext()) {
                        Order bid = bidIt.next();

                        if (bid.getTriggerPrice().compareTo(latestPrice) >= 0) {
                            that.activeStopOrderQueue.add(bid);
                            bidIt.remove();
                        } else {
                            break;
                        }
                    }

                    Iterator<Order> askIt = book.getSellStopOrders().iterator();
                    while (askIt.hasNext()) {
                        Order ask = askIt.next();

                        if (ask.getTriggerPrice().compareTo(latestPrice) <= 0) {
                            that.activeStopOrderQueue.add(ask);
                            askIt.remove();
                        } else {
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 创建下单队列
        this.addOrderQueue = DisruptorQueueFactory.createQueue(2 << 16, new AbstractDisruptorConsumer<Order>() {
            @Override
            public void process(Order event) {

                //
                // 确保每一个订单的撮合都是独立的
                //
                try {
                    that.addOrderInternal(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 创建激活止盈止损订单队列
        this.activeStopOrderQueue = DisruptorQueueFactory.createQueue(64, new AbstractDisruptorConsumer<Order>() {
            @Override
            public void process(Order event) {

                //
                // 确保每一个订单的撮合都是独立的
                //
                try {
                    that.activeStopOrder(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 添加订单
     *
     * @param order
     *         订单
     */
    public void addOrder(Order order) {
        this.addOrderQueue.add(order);
    }

    /**
     * 取消一个订单
     *
     * @param orderId 订单ID
     */
    public void cancelOrder (String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            throw new TradeException("非法订单ID");
        }
        Order order = this.orderMgr.getOrder(orderId);
        synchronized (order.getId()) {
            if (order.isCanceled()) {
                throw new TradeException("该订单已经取消");
            }
            if (order.isFinished()) {
                throw new TradeException("该订单已经结束");
            }

            // 设置订单取消标记位
            order.markCanceled();
        }
    }

    /**
     * 添加订单
     *
     * @param order
     *         订单
     */
    private void addOrderInternal(Order order) {
        OrderBook book = Objects.requireNonNull(this.bookMgr.getBook(order));
        this.orderMgr.addOrder(order);
        book.addOrder(order);

        // 添加订单
        this.executeHandler(h -> {
            try {
                h.onAddOrder(order);
            } catch (Exception e) {
                this.orderMgr.removeOrder(order);
                throw new TradeException(e.getMessage());
            }
        });

        // 立马执行撮合
        if (this.isMatching()) {
            matchOrder(book, order);
        }
    }

    /**
     * 止盈止损订单激活
     *
     * @param stopOrder
     *         止盈止损订单
     */
    private void activeStopOrder(Order stopOrder) {
        OrderBook book = Objects.requireNonNull(this.bookMgr.getBook(stopOrder));

        // 设置订单激活标记
        stopOrder.setActivated(true);

        // 账本激活止盈止损订单
        // 也就是将止盈利止损订单放入撮合买卖盘
        book.activeStopOrder(stopOrder);

        // 添加订单
        this.executeHandler(h -> {
            try {
                h.onActiveStopOrder(stopOrder);
            } catch (Exception e) {
                stopOrder.setActivated(false);
                //
                // 如果激活止盈止损订单失败, 则直接重新将订单放入止盈止损列表中
                //
                book.addOrder(stopOrder);
                throw new TradeException(e.getMessage());
            }
        });

        // 立马执行撮合
        if (this.isMatching()) {
            matchOrder(book, stopOrder);
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
    @Synchronized
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

        // 构建上下文
        this.buildMatchingContext(book);

        while (opponentIt.hasNext()) {
            Order best = opponentIt.next();

            // 查找订单匹配器
            Matcher matcher = this.lookupMatcher(order, best);

            if (matcher == null) {
                return;
            }

            // 将查找到的匹配器设置到匹配上下文中
            this.resetMatcherToContext(matcher);

            //
            // 订单结束状态补偿
            //
            if (matcher.isFinished(order)) {
                order.markFinished();
                return;
            }

            if (matcher.isFinished(best)) {
                best.markFinished();

                // 推送事件
                this.executeOrderCancel(best);

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
            TradeResult ts = matcher.doTrade(order, best);

            //
            // 事务
            //
            Order snap_order = order.snap();
            Order snap_best = best.snap();

            // NOTE TEST ONLY
            if (this.isEnableLog) {
                log.info(book.render_bid_ask());
                log.info(book.render_depth_chart());
            }

            //
            // 处理订单撮合结果
            //
            this.executeHandler((handler) -> {
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
                    throw new TradeException(e.getMessage());
                }
            });

            // 移除已经结束的订单
            if (matcher.isFinished(best)) {

                // 标记订单已结束
                best.markFinished();

                // 直接使用
                opponentIt.remove();

                // 推送事件
                this.executeOrderCancel(best);

                if (order.isBuy()) {
                    opponentIt = book.getAskOrders().iterator();
                } else {
                    opponentIt = book.getBidOrders().iterator();
                }
            }

            // 撮合结束
            if (matcher.isFinished(order)) {
                //
                // 标记已经结束的订单并且结束撮合
                //
                order.markFinished();
                return;
            }
        }
    }

    /**
     * 构建匹配上下文
     *
     * @param book
     *         book
     */
    private void buildMatchingContext(OrderBook book) {
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_CONTEXT, ThreadLocalMatchingContext.INSTANCE);
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MARKET_MANAGER, marketMgr);
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_ORDER_BOOK, book);
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MATCH_ENGINE, this);
    }

    /**
     * 设置上下文的匹配器
     *
     * @param matcher
     *         匹配器
     */
    private void resetMatcherToContext(Matcher matcher) {
        ThreadLocalUtils.set(ThreadLocalMatchingContext.NAME_OF_MATCHER, matcher);
    }

    /**
     * 添加一个匹配器
     *
     * @param matcher
     *         匹配器
     */
    public void addMatcher(Matcher matcher) {
        this.matchers.add(Objects.requireNonNull(matcher));
    }

    /**
     * 根据订单搜索合适的匹配器, 如果没有找到合适的匹配器那么则返回 {@code null}
     *
     * @param order
     *         订单
     * @param opponentOrder
     *         对手订单
     *
     * @return 匹配器
     */
    private Matcher lookupMatcher(Order order, Order opponentOrder) {
        return this.matchers.stream()
                            .filter(matcher -> matcher.isSupport(order, opponentOrder))
                            .findFirst()
                            .orElse(null);
    }

    /**
     * 添加一个事件处理器
     *
     * @param h
     *         {@link MatchHandler}
     */
    public void addHandler(MatchHandler h) {
        Objects.requireNonNull(h, "handler is null");
        this.handlers.add(h);

        // 排序
        this.handlers.sort(Comparator.comparing(MatchHandler::getPriority).reversed());
    }

    /**
     * 执行处理器,当其中任意一个处理失败的时, 其后续的处理器将不会继续执行
     *
     * @param f
     *         handler 消费者
     *
     * @throws Exception
     */
    private void executeHandler(Consumer<MatchHandler> f) {
        for (int i = 0; i < this.handlers.size(); i++) {
            MatchHandler h = this.handlers.get(i);
            f.accept(h);
        }
    }

    /**
     * 执行订单取消移除事件
     *
     * @param order
     *         已经移除的订单
     */
    private void executeOrderCancel(Order order) {
        this.executeHandler((h) -> {
            h.onOrderCancel(order);
        });
    }

    /**
     * 是否正在撮合
     *
     * @return 是否正在撮合
     */
    public boolean isMatching() {
        return this.isMatching;
    }

    /**
     * 停止撮合
     */
    public void disableMatching() {
        this.isMatching = true;
    }

    /**
     * 开启撮合
     */
    public void enableMatching() {
        this.isMatching = true;
    }

    /**
     * 开启日志
     */
    public void enableLog() {
        this.isEnableLog = true;
    }

    /**
     * 关闭日志
     */
    public void disableLog() {
        this.isEnableLog = false;
    }
}
