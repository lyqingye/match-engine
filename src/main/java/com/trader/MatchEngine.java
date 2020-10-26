package com.trader;

import com.trader.book.OrderRouter;
import com.trader.book.Scheduler;
import com.trader.def.Cmd;
import com.trader.entity.Order;
import com.trader.exception.TradeException;
import com.trader.market.MarketManager;
import com.trader.support.OrderBookManager;
import com.trader.support.OrderManager;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
     * 调度器
     */
    private Scheduler scheduler;

    /**
     * 路由
     */
    private OrderRouter router;

    /**
     * 下单队列
     */
    private DisruptorQueue<Order> addOrderQueue;

    public MatchEngine() {
        this.orderMgr = new OrderManager();
        this.bookMgr = new OrderBookManager();
        this.marketMgr = new MarketManager(bookMgr);

        // 创建下单队列
        this.addOrderQueue = DisruptorQueueFactory.createQueue(2 << 16, new AbstractDisruptorConsumer<Order>() {
            @Override
            public void process(Order event) {

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

        if (order != null) {
            synchronized (order.getId()) {
                if (order.isCanceled()) {
                    return;
                }

                if (order.isFinished()) {
                    return;
                }

                // 设置订单取消标记位
                order.markCanceled();

                // 添加进队列等待取消
                order.setCmd(Cmd.CANCEL_ORDER);
                this.addOrder(order);
            }
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
     * 是否开启了日志
     *
     * @return 是否开启了日志
     */
    public boolean isEnableLog() {
        return this.isEnableLog;
    }

    /**
     * 关闭日志
     */
    public void disableLog() {
        this.isEnableLog = false;
    }
}
