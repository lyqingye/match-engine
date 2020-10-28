package com.trader;

import com.trader.config.MatchEngineConfig;
import com.trader.core.OrderRouter;
import com.trader.core.Scheduler;
import com.trader.core.support.router.GenericOrderRouter;
import com.trader.core.support.scheduler.GenericScheduler;
import com.trader.def.Cmd;
import com.trader.entity.Order;
import com.trader.exception.TradeException;
import com.trader.market.MarketManager;
import com.trader.matcher.MatcherManager;
import com.trader.matcher.limit.InMemoryLimitMatchHandler;
import com.trader.matcher.limit.LimitOrderMatcher;
import com.trader.matcher.market.InMemoryMarketMatchHandler;
import com.trader.matcher.market.MarketOrderMatcher;
import com.trader.support.OrderManager;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

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
     * 订单管理器
     */
    @Getter
    private OrderManager orderMgr;

    /**
     * 调度器
     */
    @Getter
    private Scheduler scheduler;

    /**
     * 市场管理器
     */
    @Getter
    private MarketManager marketMgr;


    /**
     * 下单队列
     */
    private DisruptorQueue<Order> addOrderQueue;

    public static MatchEngine newEngine(MatchEngineConfig config) {
        // 订单路由
        OrderRouter router;
        if (config.getRouter() != null) {
            router = config.getRouter();
        } else {
            router = new GenericOrderRouter();
        }
        config.setRouter(router);

        // 撮合处理器
        CompositeMatchEventHandler handlers = new CompositeMatchEventHandler(new InMemoryLimitMatchHandler(),
                                                                             new InMemoryMarketMatchHandler());
        MatchHandler h = config.getHandler();
        if (h != null) {
            handlers.regHandler(h);
        }

        // 市场管理器
        MarketManager market = new MarketManager(config);
        handlers.regHandler(market.getMatchHandler());

        // 撮合规则
        MatcherManager matcher = new MatcherManager();
        matcher.addMatcher(new LimitOrderMatcher());
        matcher.addMatcher(new MarketOrderMatcher());

        // 调度器
        GenericScheduler scheduler = new GenericScheduler(router,
                                                          matcher,
                                                          market,
                                                          handlers,
                                                          config.getNumberOfCores(),
                                                          config.getSizeOfCoreCmdBuffer());
        config.setScheduler(scheduler);
        return new MatchEngine(market, scheduler, config.getSizeOfOrderQueue());
    }

    public MatchEngine(MarketManager market,
                       Scheduler scheduler,
                       int sizeOfOrderQueue) {
        this.orderMgr = new OrderManager();
        this.scheduler = Objects.requireNonNull(scheduler);
        this.marketMgr = Objects.requireNonNull(market);

        // 创建下单队列
        this.addOrderQueue = DisruptorQueueFactory.createQueue(sizeOfOrderQueue, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread tr = new Thread(r);
                tr.setName("MatchEngine:AddOrderQueue");
                return tr;
            }
        }, new AbstractDisruptorConsumer<Order>() {
            @Override
            public void process(Order event) {
                while (!isMatching) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                scheduler.submit(event);
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
     * @param orderId
     *         订单ID
     */
    public void cancelOrder(String orderId) {
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
        this.isMatching = false;
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
