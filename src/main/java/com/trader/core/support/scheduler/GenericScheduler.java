package com.trader.core.support.scheduler;

import com.trader.core.MatchHandler;
import com.trader.core.OrderRouter;
import com.trader.core.Scheduler;
import com.trader.core.entity.Order;
import com.trader.core.exception.MatchExceptionHandler;
import com.trader.core.matcher.MatcherManager;
import com.trader.core.support.processor.GenericProcessor;
import com.trader.market.MarketEventHandler;
import com.trader.market.MarketManager;
import com.trader.market.publish.msg.PriceChangeMessage;

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
    private final Map<String, GenericProcessor> processorCache;

    /**
     * order router
     */
    private final OrderRouter router;

    /**
     * 匹配器管理器
     */
    private final MatcherManager matcherMgr;

    /**
     * 市场管理器
     */
    private final MarketManager marketMgr;

    /**
     * 撮合结果处理器
     */
    private final MatchHandler matchHandler;

    /**
     * 最大处理器个数
     */
    private final int maxNumOfProcessors;

    /**
     * 处理器命令缓冲区大小
     */
    private final int sizeOfProcessorCmdBuffer;

    /**
     * 异常处理
     */
    private MatchExceptionHandler matchExceptionHandler;

    /**
     * 实际的任务个数
     */
    private int actualNumOfTask = 0;

    /**
     * 是否正在运行
     */
    private volatile boolean isRunning = false;

    public GenericScheduler(OrderRouter router,
                            MatcherManager matcherMgr,
                            MarketManager marketMgr,
                            MatchHandler matchHandler,
                            MatchExceptionHandler matchExceptionHandler,
                            int maxNumOfProcessors,
                            int sizeOfProcessorCmdBuffer) {
        this.maxNumOfProcessors = maxNumOfProcessors;
        this.router = Objects.requireNonNull(router);
        this.matcherMgr = Objects.requireNonNull(matcherMgr);
        this.marketMgr = Objects.requireNonNull(marketMgr);
        this.matchHandler = Objects.requireNonNull(matchHandler);
        this.matchExceptionHandler = Objects.requireNonNull(matchExceptionHandler);
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

        this.isRunning = true;
    }

    /**
     * 提交需要调度的订单及订单簿
     *
     * @param order
     *         订单
     */
    @Override
    public void submit(Order order) {
        if (isRunning) {
            GenericProcessor processor;
            if (processorCache.containsKey(order.getSymbol())) {
                processor = processorCache.get(order.getSymbol());
            } else {
                actualNumOfTask++;
                String theProcessorName = order.getSymbol();
                // 当前处理器已经满了，则将其映射到任意一个处理器
                if (processorCache.size() >= maxNumOfProcessors) {
                    processor = (GenericProcessor) processorCache.values()
                            .toArray()[actualNumOfTask % maxNumOfProcessors];
                    processor.renaming(processor.name() + " | " + theProcessorName);
                } else {
                    // 创建一个处理器
                    processor = new GenericProcessor(theProcessorName,
                            router,
                            matcherMgr,
                            marketMgr,
                            matchExceptionHandler,
                            sizeOfProcessorCmdBuffer);

                    processor.regHandler(matchHandler);
                }
                processorCache.put(order.getSymbol(),
                        processor);
            }
            processor.exec(order);
        }
    }

    /**
     * 调度器销毁
     */
    @Override
    public void shutdownAndWait() {
        // 停止所有处理器
        processorCache.values().forEach(GenericProcessor::shutdownAndWait);
    }

    private void triggerMarketPriceChange(PriceChangeMessage msg) {
        if (isRunning) {
            GenericProcessor processor = processorCache.get(msg.getSymbol());
            if (processor != null) {
                processor.execPriceChange(msg);
            }
        }
    }
}
