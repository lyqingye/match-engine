package com.trader.core.entity;

import com.trader.core.comprator.AskComparator;
import com.trader.core.comprator.BidComparator;
import com.trader.core.comprator.StopAskComparator;
import com.trader.core.comprator.StopBidComparator;
import com.trader.core.def.OrderType;
import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.entity.MarketDepthInfo;
import com.trader.utils.MarketDepthUtils;
import com.trader.utils.SymbolUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;


/**
 * @author yjt
 * @since 2020/9/1 上午10:12
 */
@Data
public class OrderBook {

    /**
     * 订单簿权重, 当权重越高该订单簿撮合性能越快
     */
    private int weight;

    /**
     * 交易对
     */
    private String symbolId;

    /**
     * 买入订单
     */
    private TreeSet<Order> bidOrders = new TreeSet<>(BidComparator.getInstance());

    /**
     * 卖出订单
     */
    private TreeSet<Order> askOrders = new TreeSet<>(AskComparator.getInstance());

    /**
     * 止盈止损 (买入)
     */
    private TreeSet<Order> buyStopOrders = new TreeSet<>(StopBidComparator.getInstance());

    /**
     * 止盈止损 (卖出)
     */
    private TreeSet<Order> sellStopOrders = new TreeSet<>(StopAskComparator.getInstance());

    /**
     * 最后一条成交价
     */
    private volatile BigDecimal lastTradePrice = BigDecimal.ZERO;

    /**
     * 最后一次更新的时间
     */
    private volatile long lastTradeTime = 0;


    /**
     * 下单
     *
     * @param o
     */
    public void addOrder(Order o) {
        Objects.requireNonNull(o, "order is null");

        switch (o.getType()) {
            /**
             * 市价单和限价单
             */
            case MARKET:
            case LIMIT: {
                if (o.isBuy()) {
                    bidOrders.add(o);
                } else {
                    askOrders.add(o);
                }
                break;
            }

            case STOP: {

                /**
                 * 止盈止损单
                 */
                if (o.isBuy()) {
                    buyStopOrders.add(o);
                } else {
                    sellStopOrders.add(o);
                }
                break;
            }

            default: {
                throw new IllegalArgumentException("invalid order type");
            }
        }
    }

    /**
     * 激活一个止盈止损订单
     *
     * @param stopOrder
     *         止盈止损订单
     */
    public void activeStopOrder(Order stopOrder) {
        if (stopOrder.isBuy()) {
            bidOrders.add(stopOrder);
        } else {
            askOrders.add(stopOrder);
        }
    }

    /**
     * 从账本移除一个订单
     *
     * @param o
     *         需要移除的订单
     */
    public void removeOrder(Order o) {
        TreeSet<Order> orders = null;
        if (o.isBuy()) {
            orders = bidOrders;
        } else {
            orders = askOrders;
        }
        orders.removeIf(v -> v.getId().equals(o.getId()));
    }

    /**
     * 移除已经待激活的止盈止损订单
     *
     * @param stopOrder
     *         止盈止损订单
     */
    public void removeWaitActiveStopOrder(Order stopOrder) {
        if (stopOrder.isStopOrder()) {
            TreeSet<Order> orders = null;

            if (stopOrder.isBuy()) {
                orders = buyStopOrders;
            } else {
                orders = sellStopOrders;
            }
            orders.removeIf(v -> v.getId().equals(stopOrder.getId()));
        }
    }

    /**
     * 快照买卖盘 （全部深度）
     *
     * @param limit
     *         大小
     *
     * @return 买卖盘
     */
    public MarketDepthChartSeries snapSeries(int limit) {
        final MarketDepthChartSeries series = new MarketDepthChartSeries();
        final DepthLevel[] levels = DepthLevel.values();
        series.setSymbol(SymbolUtils.toGenericSymbol(this.symbolId));
        series.setSeries(new ArrayList<>(levels.length));

        // 买盘
        List<MarketDepthInfo> bids = new ArrayList<>(bidOrders.size());

        // 卖盘
        List<MarketDepthInfo> asks = new ArrayList<>(askOrders.size());

        // 获取买卖盘
        for (Order bid : this.bidOrders) {
            // 忽略市价订单
            if (bid.isFinished() || bid.isCanceled() ||
                    OrderType.MARKET.equals(bid.getType()) ||
                    bid.getLeavesAmount().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            MarketDepthInfo dep = new MarketDepthInfo();
            // 已经成交量
            dep.setExecuted(bid.getExecutedQuantity());

            // 总量 = 总金额 / 单价
            dep.setTotal(bid.getTotalAmount().divide(bid.getPrice(), RoundingMode.DOWN));

            // 单价
            dep.setPrice(bid.getPrice());

            // 剩余量 = 剩余金额 / 单价
            dep.setLeaves(bid.getLeavesAmount().divide(bid.getPrice(), RoundingMode.DOWN));
            bids.add(dep);
        }

        for (Order ask : this.askOrders) {
            // 忽略市价订单
            if (ask.isFinished() || ask.isCanceled() ||
                    OrderType.MARKET.equals(ask.getType()) ||
                    ask.getLeavesQuantity().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            MarketDepthInfo dep = new MarketDepthInfo();

            // 成交量
            dep.setExecuted(ask.getExecutedQuantity());

            // 总数量
            dep.setTotal(ask.getQuantity());

            // 单价
            dep.setPrice(ask.getPrice());

            // 剩余量
            dep.setLeaves(ask.getLeavesQuantity());
            asks.add(dep);
        }
        for (DepthLevel depth : levels) {
            MarketDepthChart chart = new MarketDepthChart();
            chart.setDepth(depth);

            // 卖单升序
            chart.setAsks(MarketDepthUtils.fastRender(asks, depth, limit, MarketDepthInfo::compareTo));

            // 买单降序
            chart.setBids(MarketDepthUtils.fastRender(bids, depth, limit, MarketDepthInfo::reverseCompare));
            series.getSeries().add(chart);
        }
        return series;
    }

    /**
     * 获取最后一条成交价格
     *
     * @return 成交价格
     */
    public BigDecimal getLastTradePrice() {
        return this.lastTradePrice;
    }

    /**
     * 更新最后一条成交价格
     *
     * @param newPrice
     *         最后一条成交价格
     *
     * @return 成交价格
     */
    public BigDecimal updateLastTradePrice(BigDecimal newPrice) {
        BigDecimal old = this.lastTradePrice;
        this.lastTradePrice = Objects.requireNonNull(newPrice).setScale(8, BigDecimal.ROUND_DOWN);
        this.lastTradeTime = System.currentTimeMillis();
        return old;
    }
}
