package com.trader.core.support.scheduler;

import com.trader.MatchHandler;
import com.trader.core.OrderRouter;
import com.trader.core.Scheduler;
import com.trader.core.support.processor.GenericProcessor;
import com.trader.entity.Order;
import com.trader.market.MarketEventHandler;
import com.trader.market.MarketManager;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.matcher.MatcherManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 处理器命令缓冲区大小
     */
    private int sizeOfProcessorCmdBuffer;

    public GenericScheduler(OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr,
                            MatchHandler matchHandler,
                            int maxNumOfProcessors,
                            int sizeOfProcessorCmdBuffer) {
        this.maxNumOfProcessors = maxNumOfProcessors;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = Objects.requireNonNull(marketMgr);
        this.matchHandler = Objects.requireNonNull(matchHandler);
        this.processorCache = new HashMap<>(maxNumOfProcessors);
        this.sizeOfProcessorCmdBuffer = sizeOfProcessorCmdBuffer;

        marketMgr.addHandler(new MarketEventHandler() {
            /**
             * 市价价格变动事件
             *
             * @param msg
             */
            @Override
            public void onMarketPriceChange(PriceChangeMessage msg) {
                triggerMarketPriceChange(msg);
            }
        });
    }

    /**
     * 提交需要调度的订单及订单簿
     *
     * @param order
     *         订单
     */
    AtomicInteger counter = new AtomicInteger(0);
    @Override
    public void submit(Order order) {
        GenericProcessor processor;
        if (processorCache.containsKey(order.getSymbol())) {
            processor = processorCache.get(order.getSymbol());
        } else {
            String theProcessorName = order.getSymbol();
            // 当前处理器已经满了，则将其映射到任意一个处理器
            if (processorCache.size() >= maxNumOfProcessors) {
                final int idx = order.getSymbol().hashCode() % (this.maxNumOfProcessors - 1);
                processor = (GenericProcessor) processorCache.values()
                                                             .toArray()[idx];
                processor.renaming(processor.name() + " | " + theProcessorName);
            } else {
                // 创建一个处理器
                processor = new GenericProcessor(theProcessorName,
                                                 router,
                                                 matcherMgr,
                                                 marketMgr,
                                                 sizeOfProcessorCmdBuffer);
                processor.regHandler(matchHandler);
            }
            processorCache.put(order.getSymbol(),
                               processor);
        }
        processor.exec(order);
        System.out.println(counter.incrementAndGet());
    }

    private void triggerMarketPriceChange(PriceChangeMessage msg) {
        GenericProcessor processor = processorCache.get(msg);
        if (processor != null) {
            processor.execPriceChange(msg);
        }
    }
}
