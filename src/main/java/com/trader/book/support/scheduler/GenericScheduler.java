package com.trader.book.support.scheduler;

import com.trader.MatchHandler;
import com.trader.book.OrderRouter;
import com.trader.book.Scheduler;
import com.trader.book.support.processor.GenericProcessor;
import com.trader.entity.Order;
import com.trader.market.MarketManager;
import com.trader.matcher.MatcherManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 线程池调度器, 并且一个处理器控制一个交易对
 *
 * @author yjt
 * @since 2020/10/23 上午10:03
 */
public class GenericScheduler implements Scheduler {

    /**
     * symbol -> processor
     */
    private Map<String, GenericProcessor> processorCache;

    /**
     * order router
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
     * 撮合结果处理器
     */
    private MatchHandler matchHandler;

    /**
     * 最大处理器个数
     */
    private int maxNumOfProcessors;

    public GenericScheduler(OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr,
                            MatchHandler matchHandler,
                            int maxNumOfProcessors) {
        this.maxNumOfProcessors = maxNumOfProcessors;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = Objects.requireNonNull(marketMgr);
        this.matchHandler  = Objects.requireNonNull(matchHandler);
        this.processorCache = new HashMap<>(maxNumOfProcessors);
    }

    /**
     * 提交需要调度的订单及订单簿
     *
     * @param order
     *         订单
     */
    @Override
    public void submit(Order order) {
        GenericProcessor processor;
        if (processorCache.containsKey(order.getSymbol())) {
            processor = processorCache.get(order.getSymbol());
        } else {
            String theProcessorName = order.getSymbol();
            // 当前处理器已经满了，则将其映射到任意一个处理器
            if (processorCache.size() >= maxNumOfProcessors) {
                final int idx = order.getSymbol().hashCode() % this.maxNumOfProcessors;
                processor = (GenericProcessor) processorCache.values()
                                                             .toArray()[idx];
                processor.renaming(processor.name() + " | " + theProcessorName);
            } else {
                // 创建一个处理器
                processor = new GenericProcessor(theProcessorName,
                                                 router,
                                                 matcherMgr,
                                                 marketMgr,
                                                 64);
                processor.regHandler(matchHandler);
            }
            processorCache.put(order.getSymbol(),
                               processor);
        }
        processor.exec(order);
    }
}
