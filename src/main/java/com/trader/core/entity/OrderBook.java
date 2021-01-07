package com.trader.core.entity;

import com.trader.core.comprator.AskComparator;
import com.trader.core.comprator.BidComparator;
import com.trader.core.comprator.StopAskComparator;
import com.trader.core.comprator.StopBidComparator;
import com.trader.core.def.Category;
import com.trader.core.def.OrderType;
import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.entity.MarketDepthInfo;
import com.trader.utils.MarketDepthUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

import static com.trader.utils.ArithmeticUtils.*;


/**
 * @author yjt
 * @since 2020/9/1 上午10:12
 */
@Data
public class OrderBook {
    // 交易对
    private String symbolId;
    // 标签
    private Category category;
    // 是否推送深度
    private boolean publishDepth;
    // 是否推送k线
    private boolean publishKline;
    // 买入订单
    private TreeSet<Order> bidLimitOrders = new TreeSet<>(BidComparator.getInstance());
    // 卖出订单
    private TreeSet<Order> askLimitOrders = new TreeSet<>(AskComparator.getInstance());
    // 止盈止损 (买入)
    private TreeSet<Order> bidStopOrders = new TreeSet<>(StopBidComparator.getInstance());
    // 止盈止损 (卖出)
    private TreeSet<Order> askStopOrders = new TreeSet<>(StopAskComparator.getInstance());
    // 最后一条成交价
    private volatile BigDecimal lastTradePrice = BigDecimal.ZERO;
    // 最后一次更新的时间
    private volatile long lastTradeTime = 0;

    public void addOrder(Order o) {
        Objects.requireNonNull(o, "order is null");
        switch (o.getType()) {
            case MARKET:
            case LIMIT: {
                if (o.isBuy()) {
                    bidLimitOrders.add(o);
                } else {
                    askLimitOrders.add(o);
                }
                break;
            }
            case STOP: {
                if (o.isBuy()) {
                    bidStopOrders.add(o);
                } else {
                    askStopOrders.add(o);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid order type");
            }
        }
    }

    public Iterator<Order> askLimitOrderIt () {
        return askLimitOrders.iterator();
    }

    public Iterator<Order> bidLimitOrderIt ()  {
        return bidLimitOrders.iterator();
    }

    public Iterator<Order> askStopOrderIt () {
        return askStopOrders.iterator();
    }

    public Iterator<Order> bidStopOrderI () {
        return bidStopOrders.iterator();
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
            orders = bidLimitOrders;
        } else {
            orders = askLimitOrders;
        }
        orders.removeIf(v -> v.getId().equals(o.getId()));
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
        series.setSymbol(this.symbolId);
        series.setSeries(new ArrayList<>(levels.length));

        // 买盘
        List<MarketDepthInfo> bids = new ArrayList<>(bidLimitOrders.size());

        // 卖盘
        List<MarketDepthInfo> asks = new ArrayList<>(askLimitOrders.size());

        // 获取买卖盘
        for (Order bid : this.bidLimitOrders) {
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
            dep.setTotal(div(bid.getTotalAmount(),bid.getPrice()));

            // 单价
            dep.setPrice(bid.getPrice());

            // 剩余量 = 剩余金额 / 单价
            dep.setLeaves(div(bid.getLeavesAmount(),bid.getPrice()));
            bids.add(dep);
        }

        for (Order ask : this.askLimitOrders) {
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
     * @return 旧成交价格
     */
    public BigDecimal updateLastTradePrice(BigDecimal newPrice) {
        BigDecimal old = this.lastTradePrice;
        this.lastTradePrice = round(Objects.requireNonNull(newPrice));
        this.lastTradeTime = System.currentTimeMillis();
        return old;
    }
}
