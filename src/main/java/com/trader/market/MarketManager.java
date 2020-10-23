package com.trader.market;

import com.trader.MatchHandler;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.helper.tuples.Tuple;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.publish.MarketPublishHandler;
import com.trader.market.publish.msg.Message;
import com.trader.market.publish.msg.MessageType;
import com.trader.market.publish.msg.PriceChangeMessage;
import com.trader.matcher.TradeResult;
import com.trader.support.OrderBookManager;
import com.trader.utils.SymbolUtils;
import com.trader.utils.ThreadPoolUtils;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * 账本管理器
     */
    private OrderBookManager orderBookManager;

    /**
     * 市场事件处理器
     */
    private List<MarketEventHandler> handlers = new ArrayList<>(16);


    /**
     * hide default constructor
     */
    private MarketManager() {
    }


    public MarketManager(OrderBookManager orderBookManager) {
        this.orderBookManager = Objects.requireNonNull(orderBookManager);

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
        OrderBook book = orderBookManager.getBook(order);
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
        OrderBook book = orderBookManager.getBook(symbol);
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
        OrderBook book = orderBookManager.getBook(SymbolUtils.makeSymbol(coinId, currencyId));
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
            OrderBook book = orderBookManager.getBook(SymbolUtils.makeSymbol(tuple));
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
                    this.orderBookManager.getBook(symbol)
                                         .updateLastTradePrice(new BigDecimal(price));
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
        if (json == null) return;
        MessageType type = Message.getTypeFromJson(json);
        if (type == null) return;
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
                    this.orderBookManager.getBook(msg.getSymbol())
                                         .updateLastTradePrice(msg.getPrice());

                    // 触发事件
                    this.syncExecuteHandler(h -> {
                        h.onMarketPriceChange(msg.getSymbol(),
                                              msg.getPrice(),
                                              msg.getThird());
                    });
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
     * (异步) 执行事件处理器
     *
     * @param hConsumer
     *         处理器消费者 {@link MarketEventHandler}
     */
    private void asyncExecuteHandler(Consumer<MarketEventHandler> hConsumer) {
        ThreadPoolUtils.submit(() -> {
            this.handlers.forEach(hConsumer);
        });
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

        OrderBook book = orderBookManager.getBook(newOrder);
        final MarketDepthChartSeries series = book.snapSeries(20);
        // 异步处理市场管理器事件
        this.syncExecuteHandler((h) -> {
            // 推送盘口
            h.onDepthChartChange(series);
        });

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
        OrderBook book = orderBookManager.getBook(order);
        final MarketDepthChartSeries series = book.snapSeries(20);

        // 更新成交价价
        book.updateLastTradePrice(ts.getExecutePrice());

        // 异步处理市场管理器事件
        this.asyncExecuteHandler((h) -> {

            // 推送交易数据
            h.onTrade(order.getSymbol(),
                      order.getSide(),
                      ts
            );
            h.onDepthChartChange(series);
        });

        // 上面这个事件和下面这个事件不应该放在一起推送
        // 因为上面这个影响的是市场数据
        // 下面这个会影响止盈止损挂单的数据
        // 所以分开触发
        this.syncExecuteHandler((h) -> {
            // 推送市价变动事件
            h.onMarketPriceChange(order.getSymbol(),
                                  ts.getExecutePrice(), false);
        });
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
        OrderBook book = orderBookManager.getBook(removed);
        final MarketDepthChartSeries series = book.snapSeries(20);

        // 异步处理市场管理器事件
        this.asyncExecuteHandler((h) -> {
            h.onDepthChartChange(series);
        });
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
        OrderBook book = orderBookManager.getBook(stopOrder);
        final MarketDepthChartSeries series = book.snapSeries(20);

        // 异步处理市场管理器事件
        this.asyncExecuteHandler((h) -> {
            h.onDepthChartChange(series);
        });
    }
}
