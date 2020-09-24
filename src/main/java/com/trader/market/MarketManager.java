package com.trader.market;

import com.trader.MatchHandler;
import com.trader.ThreadPool;
import com.trader.entity.Currency;
import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;
import com.trader.matcher.TradeResult;
import com.trader.support.OrderBookManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 行情管理器
 *
 * @author yjt
 * @since 2020/9/18 下午4:34
 */
public class MarketManager implements MatchHandler{

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
    private MarketManager () {}


    public MarketManager (OrderBookManager orderBookManager) {
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
     * 添加一个处理器
     *
     * @param handler
     *         处理器
     */
    public void addHandler(MarketEventHandler handler) {
        this.handlers.add(Objects.requireNonNull(handler));
    }

    /**
     * 获取撮合引擎事件处理器
     *
     * @return 交易事件处理器
     */
    public MatchHandler getMatchHandler () {
        return this;
    }

    /**
     * (异步) 执行事件处理器
     *
     * @param hConsumer 处理器消费者 {@link MarketEventHandler}
     */
    private void asyncExecuteHandler(Consumer<MarketEventHandler> hConsumer) {
        ThreadPool.submit(() -> {
            this.handlers.forEach(hConsumer);
        });
    }

    /**
     * (同步) 执行事件处理器
     *
     * @param hConsumer 处理器消费者 {@link MarketEventHandler}
     */
    private void syncExecuteHandler(Consumer<MarketEventHandler> hConsumer) {
        ThreadPool.submit(() -> {
            this.handlers.forEach(hConsumer);
        });
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
        // 当有订单添加进来的时候, 会影响盘口的变动

        OrderBook book = orderBookManager.getBook(newOrder);

        // TODO 我们必须要将所有级别深度都进行缓存因为不同用户订阅的深度不一样
        // book.snapDepthChart()
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

        // 更新成交价价
        book.updateLastTradePrice(ts.getExecutePrice());

        // 异步处理市场管理器事件
        this.asyncExecuteHandler((h) -> {

            // 推送交易数据
            h.onTrade(order.getSymbol(),
                      order.getSide(),
                      ts.getQuantity(),
                      ts.getExecutePrice());


        });

        // 上面这个事件和下面这个事件不应该放在一起推送
        // 因为上面这个影响的是市场数据
        // 下面这个会影响止盈止损挂单的数据
        // 所以分开触发
        this.asyncExecuteHandler((h) -> {
            // 推送市价变动事件
            h.onMarketPriceChange(order.getSymbol(),
                                  ts.getExecutePrice());
        });
    }
}
