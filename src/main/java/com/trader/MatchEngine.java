package com.trader;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * TODO:
 * 1. 撮合引擎需要实现 LiftCycle 接口
 * 2. 撮合引擎需要实现事物功能,避免内存撮合刷库导致的双写数据一致性问题
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
    private List<Product> products;

    /**
     * for lookup
     */
    private Map<String,Product> productMap;

    /**
     * 委托账本列表
     * 不同的产品有不同的账本列表
     */
    private List<OrderBook> books;

    /**
     * 同上, 为了快速检索
     */
    private Map<String, OrderBook> bookMap;

    /**
     * 订单映射集合
     * K: 订单ID
     * V: 订单
     */
    private Map<String, Order> orderMap;

    /**
     * 处理器列表
     */
    private List<MatchHandler> handlers = new ArrayList<>(16);

    public void addProduct (Product product) {
        if (this.productMap.containsKey(product.getId())) {
            this.productMap.put(product.getId(),product);
        }else {
            OrderBook newBook = new OrderBook();
            newBook.setProduct(product);
            this.bookMap.put(product.getId(),newBook);
        }
    }

    public void addMarketOrder (Order order) {
        OrderBook book = bookMap.get(order.getProductId());

        if (book == null) {
            return;
        }

        Order newOrder = order.clone();
        this.orderMap.put(newOrder.getId(),newOrder);
        book.addOrder(newOrder);
        this.executeHandler(h -> {
            try {
                h.onAddOrder(newOrder);
            } catch (Exception e) {
                this.orderMap.remove(newOrder.getId());
                book.removeOrder(newOrder);
            }
        });
    }

    public void matchMarket (OrderBook book,Order order) {
        if (order.isBuy()) {
            Order bestAsk = book.getBestAsk();
            if (bestAsk == null)
                return;
            order.setPrice(bestAsk.getPrice());
        }else {
            Order bestBid = book.getBestBid();
            if (bestBid == null)
                return;
            order.setPrice(bestBid.getPrice());
        }
    }

    public void matchOrder (OrderBook book, Order order) {
        Iterator<Order> askIt = book.getAskOrders().iterator();
        Iterator<Order> bidIt = book.getBidOrders().iterator();
        while (true) {
            Order best = null;
            if (order.isBuy()) {
                if (askIt.hasNext()) {
                    best = askIt.next();
                }
            }else {
                if (bidIt.hasNext()) {
                    best = bidIt.next();
                }
            }
            if (best == null)
                return;
            boolean arbitrage = false;
            if (order.isBuy()) {
                arbitrage = order.getPrice().compareTo(best.getPrice()) >= 0;
            }else {
                arbitrage = order.getPrice().compareTo(best.getPrice()) <= 0;
            }
            if (!arbitrage)
                return;
            if (order.isAON() || order.isFOK()) {

            }

        }
    }


    /****************************************
     *              止盈止损
     ****************************************/

    /**
     * 激活一个止盈止损的单
     * @param order 订单
     * @return
     */
    private boolean activateStopOrder (Order order) {
        return false;
    }

    /**
     * 批量激活止盈止损的单
     *
     * @param orders 订单
     * @return
     */
    private boolean activateStopOrders (List<Order> orders) {
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
