package com.trader;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * 委托账本列表
     * 不同的产品有不同的账本列表
     */
    private List<OrderBook> books;

    /**
     * 同上, 为了快速检索
     */
    private Map<String, List<OrderBook>> booksMap;

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


    /**
     * 执行撮合
     */
    public void match() {
        new Thread(() -> {
            while (true) {

                // 如果引擎没有正在撮合, 则让位CPU, 线程仍处于就绪状态
                if (!isMatching) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }

                // 执行真正的撮合
                this.doMatch();
            }
        }).start();
    }

    /**
     * 真正的撮合实现
     */
    private void doMatch() {

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
    private void executeHandler(Consumer<MatchHandler> f) throws Exception {
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
