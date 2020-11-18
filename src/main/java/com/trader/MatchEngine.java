package com.trader;

import com.trader.config.MatchEngineConfig;
import com.trader.core.MatchHandler;
import com.trader.core.OrderRouter;
import com.trader.core.Scheduler;
import com.trader.core.def.ActivateStatus;
import com.trader.core.def.Cmd;
import com.trader.core.entity.Order;
import com.trader.core.exception.MatchExceptionHandler;
import com.trader.core.exception.TradeException;
import com.trader.core.handler.CompositeMatchEventHandler;
import com.trader.core.matcher.MatcherManager;
import com.trader.core.matcher.limit.InMemoryLimitMatchHandler;
import com.trader.core.matcher.limit.LimitOrderMatcher;
import com.trader.core.matcher.market.InMemoryMarketMatchHandler;
import com.trader.core.matcher.market.MarketOrderMatcher;
import com.trader.core.support.OrderManager;
import com.trader.core.support.router.GenericOrderRouter;
import com.trader.core.support.scheduler.GenericScheduler;
import com.trader.market.MarketManager;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import lombok.Getter;

import java.util.Objects;

/**
 * TODO:
 * 1. 撮合引擎需要实现 LiftCycle 接口
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
     * 异常处理器
     */
    private MatchExceptionHandler matchExceptionHandler;

    /**
     * 下单队列
     */
    private DisruptorQueue<Order> addOrderQueue;

    public static MatchEngine newEngine(MatchEngineConfig config) {
        // 异常处理器
        MatchExceptionHandler matchExceptionHandler = config.getMatchExceptionHandler();
        if (matchExceptionHandler == null) {
            matchExceptionHandler = MatchExceptionHandler.defaultHandler();
            config.setMatchExceptionHandler(matchExceptionHandler);
        }
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
                matchExceptionHandler,
                config.getNumberOfCores(),
                config.getSizeOfCoreCmdBuffer());
        config.setScheduler(scheduler);
        return new MatchEngine(market, scheduler, matchExceptionHandler, config.getSizeOfOrderQueue());
    }

    public MatchEngine(MarketManager market,
                       Scheduler scheduler,
                       MatchExceptionHandler matchExceptionHandler,
                       int sizeOfOrderQueue) {
        this.orderMgr = new OrderManager();
        this.scheduler = Objects.requireNonNull(scheduler);
        this.marketMgr = Objects.requireNonNull(market);
        this.matchExceptionHandler = Objects.requireNonNull(matchExceptionHandler);

        // 创建下单队列
        this.addOrderQueue = DisruptorQueueFactory.createQueue(sizeOfOrderQueue, r -> {
            final Thread tr = new Thread(r);
            tr.setName("MatchEngine:AddOrderQueue");
            return tr;
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
        }, matchExceptionHandler.toDisruptorHandler());
    }

    /**
     * 添加订单
     *
     * @param order
     *         订单
     */
    public void addOrder(Order order) {
        this.addOrderQueue.add(order);
        this.orderMgr.addOrder(order);
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
                if (order.isMatching()) {
                    throw new TradeException("订单已经匹配到订单, 正在结算中, 请勿撤单");
                }
                if (order.isCanceled()) {
                    return;
                }

                if (order.isFinished()) {
                    return;
                }

                if (order.isStopOrder()) {
                    order.setActivated(ActivateStatus.ACTIVATED);
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

    /**
     * 撮合引擎销毁
     */
    public void shutdown() {
        // 首先先停止入单
        disableMatching();

        // 等待剩余订单被处理
        addOrderQueue.shutdown();
        System.out.println("[MatchEngine]: add order queue shutdown finished!");

        // 停止调度器
        scheduler.shutdownAndWait();
        System.out.println("[MatchEngine]: scheduler shutdown finished!");

        // 停止消息推送
        marketMgr.shutdownAndWait();
        System.out.println("[MatchEngine]: market manager shutdown finished!");
    }
}
