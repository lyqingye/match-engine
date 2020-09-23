package com.trader;

import com.trader.context.ThreadLocalContext;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.exception.TradeException;
import com.trader.market.MarketManager;
import com.trader.matcher.TradeResult;
import com.trader.support.*;
import com.trader.utils.ThreadLocalUtils;
import lombok.Getter;
import lombok.Synchronized;

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
     * 货币管理器
     */
    @Getter
    private CurrencyManager currencyMgr;

    /**
     * 产品管理器
     */
    @Getter
    private ProductManager productMgr;

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
    private MarketManager marketMgr;

    public MatchEngine() {
        this.currencyMgr = new CurrencyManager();
        this.productMgr = new ProductManager();
        this.orderMgr = new OrderManager();
        this.bookMgr = new OrderBookManager(currencyMgr, productMgr);
        this.marketMgr = new MarketManager();
    }

    /**
     * 添加订单
     *
     * @param order
     *         订单
     */
    @Synchronized
    public void addOrder(Order order) {
        OrderBook book = this.bookMgr.getBook(order);

        if (book == null) {
            return;
        }

        Order newOrder = order.snap();
        this.orderMgr.addOrder(newOrder);
        book.addOrder(newOrder);
        // 添加订单
        this.executeHandler(h -> {
            try {
                h.onAddOrder(newOrder);
            } catch (Exception e) {
                this.orderMgr.removeOrder(order);
                book.removeOrder(newOrder);
                throw new TradeException(e.getMessage());
            }
        });

        // 立马执行撮合
        if (this.isMatching()) {
            matchOrder(book, newOrder);
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
            this.resetMatcherContext(matcher);

            if (matcher.isFinished(order) || matcher.isFinished(best)) {
                return;
            }

            // 执行撮合
            TradeResult ts = matcher.doTrade(order, best);

            //
            // 事务
            //
            Order snap_order = order.snap();
            Order snap_best = best.snap();

            // NOTE TEST ONLY
//            System.out.println(book.render_bid_ask());
//            System.out.println(book.render_depth_chart());

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
//                book.removeOrder(best);
                opponentIt.remove();
                if (order.isBuy()) {
                    opponentIt = book.getAskOrders().iterator();
                } else {
                    opponentIt = book.getBidOrders().iterator();
                }
            }

            // 撮合结束
            if (matcher.isFinished(order)) {
                //
                // 处理已经结束的订单并且结束撮合
                //
                book.removeOrder(order);

                return;
            }
        }
    }

    /**
     * 构建匹配上下文
     *
     * @param book book
     */
    private void buildMatchingContext(OrderBook book) {
        ThreadLocalUtils.set(ThreadLocalContext.NAME_OF_CONTEXT, ThreadLocalContext.INSTANCE);
        ThreadLocalUtils.set(ThreadLocalContext.NAME_OF_MARKET_MANAGER,marketMgr);
        ThreadLocalUtils.set(ThreadLocalContext.NAME_OF_ORDER_BOOK,book);
        ThreadLocalUtils.set(ThreadLocalContext.NAME_OF_MATCH_ENGINE,this);
    }

    /**
     * 设置上下文的匹配器
     *
     * @param matcher 匹配器
     */
    private void resetMatcherContext(Matcher matcher) {
        ThreadLocalUtils.set(ThreadLocalContext.NAME_OF_MATCHER,matcher);
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
