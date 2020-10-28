package com.trader.market;

import com.trader.MatchHandler;
import com.trader.config.MatchEngineConfig;
import com.trader.core.OrderRouter;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.helper.tuples.Tuple;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.msg.Message;
import com.trader.market.publish.msg.MessageType;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.market.publish.msg.TradeMessage;
import com.trader.matcher.TradeResult;
import com.trader.utils.SymbolUtils;
import com.trader.utils.ThreadPoolUtils;
import com.trader.utils.buffer.CoalescingRingBuffer;
import com.trader.utils.disruptor.AbstractDisruptorConsumer;
import com.trader.utils.disruptor.DisruptorQueue;
import com.trader.utils.disruptor.DisruptorQueueFactory;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 行情管理器
 *
 * @author yjt
 * @since 2020/9/18 下午4:34
 */
public class MarketManager implements MatchHandler {

    /**
     * 市场事件处理器
     */
    private List<MarketEventHandler> handlers = new ArrayList<>(16);

    /**
     * 订单路由
     */
    private OrderRouter router;

    /**
     * 合并型 ring buffer, 用于优化深度图推送
     * https://github.com/LMAX-Exchange/LMAXCollections
     */
    private CoalescingRingBuffer<String, MarketDepthChartSeries> depthChartRingBuffer;

    /**
     * 价格变动事件缓冲区
     */
    private CoalescingRingBuffer<String, PriceChangeMessage> priceChangeRingBuffer;

    /**
     * 深度推送队列 （仅用于多核的情况） 用于解决多线程写入
     */
    private DisruptorQueue<MarketDepthChartSeries> depthChartQueue;

    /**
     * 价格推送队列 （仅用于多核的情况） 用于解决多线程写入
     */
    private DisruptorQueue<PriceChangeMessage> priceChangeQueue;

    /**
     * 撮合结果数据推送队列
     */
    private DisruptorQueue<TradeMessage> tradeMessageQueue;

    /**
     * 撮合引擎配置
     */
    private MatchEngineConfig config;


    /**
     * hide default constructor
     */
    private MarketManager() {
    }

    public MarketManager(MatchEngineConfig config) {
        this.config = Objects.requireNonNull(config);
        this.router = Objects.requireNonNull(config.getRouter());


        //
        // 创建数据合并缓冲区
        // 该缓冲区只支持一个生产者对应一个消费者, 也就是只支持一个线程生产, 一个线程消费
        // 相关文档参阅: https://nickzeeb.wordpress.com/2013/03/07/the-coalescing-ring-buffer/
        // 这就会暴露一个问题, 当存在多和核心进行撮合的时候, 产生的事件肯定是多线程去访问的, 也就是说
        // 盘口改变事件将会被多个线程调用, 因为数据合并缓冲区只支持单线程写入, 所以在多核环境下我们需要再起两个队列
        // 以解决多线程写入
        //

        if (config.getNumberOfCores() > 1) {
            priceChangeQueue = DisruptorQueueFactory.createQueue(config.getSizeOfPublishDataRingBuffer(),
                                                                 new ThreadFactory() {
                                                                     @Override
                                                                     public Thread newThread(Runnable runnable) {
                                                                         Thread tr = new Thread(runnable);
                                                                         tr.setName("Market-Price-Change-Thread");
                                                                         return tr;
                                                                     }
                                                                 },
                                                                 new AbstractDisruptorConsumer<PriceChangeMessage>() {
                                                                     @Override
                                                                     public void process(PriceChangeMessage event) {
                                                                         priceChangeRingBuffer.offer(event.getSymbol(), event);
                                                                     }
                                                                 });

            depthChartQueue = DisruptorQueueFactory.createQueue(config.getSizeOfPublishDataRingBuffer(),
                                                                new ThreadFactory() {
                                                                    @Override
                                                                    public Thread newThread(Runnable runnable) {
                                                                        Thread tr = new Thread(runnable);
                                                                        tr.setName("Market-Depth-Change-Thread");
                                                                        return tr;
                                                                    }
                                                                },
                                                                new AbstractDisruptorConsumer<MarketDepthChartSeries>() {
                                                                    @Override
                                                                    public void process(MarketDepthChartSeries event) {
                                                                        depthChartRingBuffer.offer(event.getSymbol(), event);
                                                                    }
                                                                });
        }

        priceChangeRingBuffer = new CoalescingRingBuffer<>(config.getSizeOfPublishDataRingBuffer());
        depthChartRingBuffer = new CoalescingRingBuffer<>(config.getSizeOfPublishDataRingBuffer());

        // 启动数据合并线程
        ThreadPoolUtils.submit(() -> {
            List<PriceChangeMessage> pMessages = new ArrayList<>(16);
            List<MarketDepthChartSeries> dMessages = new ArrayList<>(16);
            for (; ; ) {

                // 合并价格数据
                priceChangeRingBuffer.poll(pMessages);
                for (PriceChangeMessage msg : pMessages) {

                    this.syncExecuteHandler(h -> {
                        h.onMarketPriceChange(msg);
                    });
                }

                // 合并深度数据
                depthChartRingBuffer.poll(dMessages);
                for (MarketDepthChartSeries msg : dMessages) {
                    // 触发事件
                    this.syncExecuteHandler(h -> {
                        h.onDepthChartChange(msg);
                    });
                }

                // 线程休眠
                try {
                    Thread.sleep(config.getPublishDataCompressCycle());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 创建撮合数据队列
        tradeMessageQueue = DisruptorQueueFactory.createQueue(config.getSizeOfTradeResultQueue(),
                                                              new AbstractDisruptorConsumer<TradeMessage>() {
                                                                  @Override
                                                                  public void process(TradeMessage event) {
                                                                      syncExecuteHandler(h -> {
                                                                          h.onTrade(event);
                                                                      });
                                                                  }
                                                              });
    }

    /**
     * 获取产品对货币的市场价格
     *
     * @param order
     *         订单
     *
     * @return 市场价格
     */
    public BigDecimal getMarketPrice(Order order) {
        OrderBook book = router.routeToBookForQueryPrice(order);
        return book.getLastTradePrice();
    }

    /**
     * 获取产品对货币的市场价格
     *
     * @param symbol
     *         交易对
     *
     * @return 市场价格
     */
    public BigDecimal getMarketPrice(String symbol) {
        OrderBook book = router.routeToBookForQueryPrice(symbol);
        return book.getLastTradePrice();
    }

    /**
     * 获取产品对货币的市场价格
     *
     * @param coinId
     *         货币
     * @param currencyId
     *         交易货币
     *
     * @return 市场价格
     */
    public BigDecimal getMarketPrice(String coinId, String currencyId) {
        OrderBook book = router.routeToBookForQueryPrice(SymbolUtils.makeSymbol(coinId, currencyId));
        return book.getLastTradePrice();
    }

    /**
     * 批量获取市场价格
     *
     * @param symbol
     *         交易对元组列表 其中元组的第一个元素代表coinId 第二个元素是计价货币 currencyId
     *
     * @return 市场价格
     */
    public List<BigDecimal> getMarketPriceBatch(List<Tuple<String, String>> symbol) {
        List<BigDecimal> result = new ArrayList<>(16);
        for (Tuple<String, String> tuple : symbol) {
            OrderBook book = router.routeToBookForQueryPrice(SymbolUtils.makeSymbol(tuple));
            BigDecimal price = book.getLastTradePrice();
            if (price == null) {
                result.add(BigDecimal.ZERO);
            } else {
                result.add(price);
            }
        }
        return result;
    }

    /**
     * 添加一个处理器
     *
     * @param handler
     *         处理器
     */
    public void addHandler(MarketEventHandler handler) {
        if (handler instanceof MarketPublishHandler) {
            ((MarketPublishHandler) handler).getClient()
                                            .setConsumer(this::onThirdMarketData);
        }
        this.handlers.add(Objects.requireNonNull(handler));
    }

    /**
     * 尝试去初始化市场价格
     *
     * @param supplier
     *         提供者
     */
    public void tryToInitMarketPrice(Supplier<Map<String, String>> supplier) {
        if (supplier != null) {
            Map<String, String> map = supplier.get();
            if (map != null) {
                map.forEach((symbol, price) -> {
                    //
                    // 更新最新市场价到订单簿
                    //
                    BigDecimal p = new BigDecimal(price);
                    router.routeToNeedToUpdatePriceBook(symbol)
                          .forEach(book -> book.updateLastTradePrice(p));
                    System.out.println(String.format("[MarketEngine]: sync init market price [%s] : [%s]",
                                                     symbol, price));
                });
            }
        }
    }

    /**
     * 第三方市场数据
     *
     * @param json
     *         缓冲区
     */
    private void onThirdMarketData(JsonObject json) {
        if (json == null) {
            return;
        }
        MessageType type = Message.getTypeFromJson(json);
        if (type == null) {
            return;
        }
        json = json.getJsonObject("data");
        switch (type) {
            //
            // 市价变动
            //
            case MARKET_PRICE: {
                PriceChangeMessage msg = json.mapTo(PriceChangeMessage.class);
                if (msg != null &&
                        Boolean.TRUE.equals(msg.getThird())) {
                    System.out.println(String.format("[MarketEngine]: recv msg: [%s] {%s} {%s}",
                                                     type.name(), msg.getSymbol(), msg.getPrice().toPlainString()));
                    //
                    // 更新最新市场价到订单簿
                    //
                    router.routeToNeedToUpdatePriceBook(msg.getSymbol())
                          .forEach(book -> book.updateLastTradePrice(msg.getPrice()));


                    if (priceChangeQueue != null) {
                        priceChangeQueue.add(msg);
                    } else {
                        // 进入合并队列
                        priceChangeRingBuffer.offer(msg.getSymbol(), msg);
                    }
                }
                break;
            }
            default: {
                // ignored
            }
        }
    }

    /**
     * 获取撮合引擎事件处理器
     *
     * @return 交易事件处理器
     */
    public MatchHandler getMatchHandler() {
        return this;
    }

    /**
     * (同步) 执行事件处理器
     *
     * @param hConsumer
     *         处理器消费者 {@link MarketEventHandler}
     */
    private void syncExecuteHandler(Consumer<MarketEventHandler> hConsumer) {
        this.handlers.forEach(hConsumer);
    }


    //
    // 以下的方法是用于监听撮合引擎的撮合事件
    // 当撮合引擎有新的订单加入或者有撮合交易数据,那么就会引起
    // 行情的变动, 所以作为一个行情管理器, 我们就必须得监听撮合交易事件.
    //

    /**
     * 添加订单事件
     *
     * @param newOrder
     *         订单
     *
     * @throws Exception
     */
    @Override
    public void onAddOrder(Order newOrder) throws Exception {
        if (newOrder.isStopOrder()) {
            return;
        }

        // 当有订单添加进来的时候, 会影响盘口的变动
        OrderBook book = router.routeToBookForSendDepthChart(newOrder);

        if (book == null) {
            return;
        }
        publishDepth(book.snapSeries(20));
    }

    /**
     * 撮合订单事件
     *
     * @param order
     *         订单
     * @param opponentOrder
     *         对手订单
     * @param ts
     *         撮合结果
     *
     * @throws Exception
     */
    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder,
                               TradeResult ts) throws Exception {
        // 当有最新订单成交的时候, 需要更新最后一条成交价格
        OrderBook book = router.routeToBookForSendDepthChart(order);

        if (book == null) {
            return;
        }


        // 更新成交价价
        book.updateLastTradePrice(ts.getExecutePrice());

        // 深度写入到队列
        final MarketDepthChartSeries series = book.snapSeries(20);
        if (depthChartQueue != null) {
            depthChartQueue.add(series);
        } else {
            // 进入合并队列
            depthChartRingBuffer.offer(series.getSymbol(), series);
        }

        // 推送成交数据到队列
        final TradeMessage tradeResult = new TradeMessage();
        tradeResult.setSymbol(order.getSymbol());
        tradeResult.setQuantity(ts.getQuantity());
        tradeResult.setPrice(ts.getExecutePrice());
        tradeResult.setTs(ts.getTimestamp());
        tradeResult.setDirection(order.getSide().toDirection());
        tradeMessageQueue.add(tradeResult);

        // 进入合并队列
        PriceChangeMessage msg = new PriceChangeMessage();
        msg.setPrice(ts.getExecutePrice());
        msg.setSymbol(order.getSymbol());
        msg.setThird(false);
        if (priceChangeQueue != null) {
            priceChangeQueue.add(msg);
        } else {
            // 进入合并队列
            priceChangeRingBuffer.offer(msg.getSymbol(), msg);
        }
    }

    /**
     * 订单移除事件推送
     * <p>
     * {@inheritDoc}
     *
     * @param removed
     *         已经被移除的订单
     */
    @Override
    public void onOrderCancel(Order removed) {
        OrderBook book = router.routeToBookForSendDepthChart(removed);
        if (book == null) {
            return;
        }
        publishDepth(book.snapSeries(20));
    }

    /**
     * 激活止盈止损订单事件
     *
     * @param stopOrder
     *         止盈止损订单
     *
     * @throws Exception
     */
    @Override
    public void onActiveStopOrder(Order stopOrder) throws Exception {
        OrderBook book = router.routeToBookForSendDepthChart(stopOrder);
        if (book == null) {
            return;
        }
        publishDepth(book.snapSeries(20));
    }

    /**
     * 推送盘口
     *
     * @param series
     *         盘口
     */
    private void publishDepth(MarketDepthChartSeries series) {
        // 深度写入到队列
        if (depthChartQueue != null) {
            depthChartQueue.add(series);
        } else {
            // 进入合并队列
            depthChartRingBuffer.offer(series.getSymbol(), series);
        }
    }
}
