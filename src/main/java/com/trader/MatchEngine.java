package com.trader;

import com.trader.entity.Currency;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;
import com.trader.matcher.TradeResult;
import com.trader.support.MarketManager;

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
     * 产品列表
     */
    private List<Product> products = new ArrayList<>(16);

    /**
     * for lookup
     */
    private Map<String, Product> productMap = new HashMap<>(16);

    /**
     * 委托账本列表
     * 不同的产品有不同的账本列表
     */
    private List<OrderBook> books = new ArrayList<>(16);

    /**
     * 同上, 为了快速检索
     */
    private Map<String, OrderBook> bookMap = new HashMap<>(16);

    /**
     * 订单映射集合
     * K: 订单ID
     * V: 订单
     */
    private Map<String, Order> orderMap = new HashMap<>(16);

    /**
     * 货币映射集合
     */
    private Map<String, Currency> currencyMap = new HashMap<>(16);

    /**
     * 处理器列表
     */
    private List<MatchHandler> handlers = new ArrayList<>(16);

    /**
     * 撮合匹配器
     */
    private List<Matcher> matchers = new ArrayList<>(16);

    private MarketManager marketManager;

    public void addProduct(Product product) {
        productMap.put(product.getId(),product);
    }

    public Product getProduct (String id) {
        return productMap.get(id);
    }

    public void addCurrency(Currency currency) {
        currencyMap.put(currency.getId(),currency);
    }

    public Currency getCurrency (String id) {
        return currencyMap.get(id);
    }

    private OrderBook getBook (Order order) {
        OrderBook book = bookMap.get(order.getSymbol());
        if (book == null) {
            OrderBook newBook = new OrderBook();
            newBook.setProduct(this.getProduct(order.getProductId()));
            newBook.setCurrency(this.getCurrency(order.getCurrencyId()));
            book = newBook;
            bookMap.put(order.getSymbol(),newBook);
        }
        return book;
    }

    /**
     * 添加订单
     *
     * @param order 订单
     */
    public void addOrder(Order order) {
        OrderBook book = this.getBook(order);

        if (book == null) {
            return;
        }

        Order newOrder = order.snap();
        this.orderMap.put(newOrder.getId(), newOrder);
        book.addOrder(newOrder);

        // 添加订单
        this.executeHandler(h -> {
            try {
                h.onAddOrder(newOrder);
            } catch (Exception e) {
                this.orderMap.remove(newOrder.getId());
                book.removeOrder(newOrder);
            }
        });

        // 立马执行撮合
        if (this.isMatching()) {
            matchOrder(book, newOrder);
        }
    }

    public void matchMarket(OrderBook book, Order order) {
        if (order.isBuy()) {
            Order bestAsk = book.getBestAsk();
            if (bestAsk == null)
                return;
            order.setPrice(bestAsk.getPrice());
        } else {
            Order bestBid = book.getBestBid();
            if (bestBid == null)
                return;
            order.setPrice(bestBid.getPrice());
        }
    }

    /**
     * 撮合限价单
     *
     * @param book
     * @param order
     */
    public void matchOrder(OrderBook book, Order order) {
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

        while (opponentIt.hasNext()) {
            Order best = opponentIt.next();

            Matcher matcher = this.lookupMatcher(order, best);

            if (matcher == null) {
                return;
            }

            TradeResult ts = matcher.doTrade(order, best);

            //
            // 事务
            //
            Order snap_order = order.snap();
            Order snap_best = best.snap();

            System.out.println(book.render_bid_ask());

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

                    System.err.println(e.getMessage());
                }
            });

            // 移除已经结束的订单
            if (matcher.isFinished(best)) {
                book.removeOrder(best);

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

    public void addMatcher (Matcher matcher) {
        this.matchers.add(Objects.requireNonNull(matcher));
    }

    private Matcher lookupMatcher (Order order, Order opponentOrder) {
        return this.matchers.stream()
                            .filter(matcher -> matcher.isSupport(order, opponentOrder))
                            .findFirst()
                            .orElse(null);
    }


    /****************************************
     *              止盈止损
     ****************************************/

    /**
     * 激活一个止盈止损的单
     *
     * @param order
     *         订单
     *
     * @return
     */
    private boolean activateStopOrder(Order order) {
        return false;
    }

    /**
     * 批量激活止盈止损的单
     *
     * @param orders
     *         订单
     *
     * @return
     */
    private boolean activateStopOrders(List<Order> orders) {
        return false;
    }


    /****************************************
     *              事件处理器
     ****************************************

     /**
     * 添加一个事件处理器
     *
     * @param h {@link MatchHandler}
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


    /****************************************
     *              引擎撮合状态
     ****************************************

     /**
     * 是否正在撮合
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
}
