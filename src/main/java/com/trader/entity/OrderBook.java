package com.trader.entity;

import com.trader.comprator.AskComparator;
import com.trader.comprator.BidComparator;
import com.trader.comprator.StopAskComparator;
import com.trader.comprator.StopBidComparator;
import com.trader.def.OrderType;
import com.trader.helper.MarketDepthHelper;
import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.entity.MarketDepthInfo;
import de.vandermeer.asciitable.AsciiTable;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.trader.helper.MarketDepthHelper.render;

/**
 * @author yjt
 * @since 2020/9/1 上午10:12
 */
@Data
public class OrderBook {

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
     * 止盈止损订单锁
     */
    private ReentrantLock stopOrdersLock = new ReentrantLock();

    /**
     * 读写锁 (预留)
     */
    private ReadWriteLock reserve = new ReentrantReadWriteLock();

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
                //
                // 止盈止损单需要加锁
                //
                this.lockStopOrders();
                try {
                    /**
                     * 止盈止损单
                     */
                    if (o.isBuy()) {
                        buyStopOrders.add(o);
                    } else {
                        sellStopOrders.add(o);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    this.unlockStopOrders();
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

        // 设置订单已经被激活
        stopOrder.setActivated(true);
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
     * 锁定止盈止损订单列表
     */
    public void lockStopOrders() {
        this.stopOrdersLock.lock();
    }

    /**
     * 解锁止盈止损订单
     */
    public void unlockStopOrders() {
        this.stopOrdersLock.unlock();
    }

    /**
     * 快照买卖盘
     *
     * @return 买卖盘
     */
    public MarketDepthChart snapDepthChart(DepthLevel depth, int limit) {
        MarketDepthChart chart = new MarketDepthChart();
        chart.setDepth((byte) depth.ordinal());

        // 买盘
        List<MarketDepthInfo> bids = new ArrayList<>(bidOrders.size());

        // 卖盘
        List<MarketDepthInfo> asks = new ArrayList<>(askOrders.size());

        // 获取买卖盘
        for (Order bid : this.bidOrders) {
            // 忽略市价订单
            if (OrderType.MARKET.equals(bid.getType())) {
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
            if (OrderType.MARKET.equals(ask.getType())) {
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

        // 卖单升序
        chart.setAsks(MarketDepthHelper.fastRender(asks, depth, limit, MarketDepthInfo::compareTo));

        // 买单降序
        chart.setBids(MarketDepthHelper.fastRender(bids, depth, limit, MarketDepthInfo::reverseCompare));
        return chart;
    }

    /**
     * 快照买卖盘 （全部深度）
     *
     * @param limit 大小
     * @return 买卖盘
     */
    public MarketDepthChartSeries snapSeries (int limit) {
        final MarketDepthChartSeries series = new MarketDepthChartSeries();
        final DepthLevel[] levels = DepthLevel.values();
        series.setSymbol(this.symbolId);
        series.setSeries(new ArrayList<>(levels.length));

        // 买盘
        List<MarketDepthInfo> bids = new ArrayList<>(bidOrders.size());

        // 卖盘
        List<MarketDepthInfo> asks = new ArrayList<>(askOrders.size());

        // 获取买卖盘
        for (Order bid : this.bidOrders) {
            // 忽略市价订单
            if (OrderType.MARKET.equals(bid.getType())) {
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
            if (OrderType.MARKET.equals(ask.getType())) {
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
            chart.setDepth((byte) depth.ordinal());



            // 卖单升序
            chart.setAsks(MarketDepthHelper.fastRender(asks, depth, limit, MarketDepthInfo::compareTo));

            // 买单降序
            chart.setBids(MarketDepthHelper.fastRender(bids, depth, limit, MarketDepthInfo::reverseCompare));
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
        this.lastTradePrice = Objects.requireNonNull(newPrice);
        this.lastTradeTime = System.currentTimeMillis();
        return old;
    }

    /**
     * 可视化深度图
     *
     * @return 深度图可视化字符串
     */
    public String render_depth_chart() {
        MarketDepthChart chart = this.snapDepthChart(DepthLevel._0, 20);
        AsciiTable at = new AsciiTable();


        at.addRule();
        at.addRow("BID", "Price", "Executed", "Leaves", "Total", "-", "ASK", "Price", "Executed", "Leaves", "Total");
        at.addRule();

        Iterator<MarketDepthInfo> askIt = chart.getAsks().iterator();
        Iterator<MarketDepthInfo> bidIt = chart.getBids().iterator();

        while (bidIt.hasNext() || askIt.hasNext()) {
            MarketDepthInfo bid = bidIt.hasNext() ? bidIt.next() : null;
            MarketDepthInfo ask = askIt.hasNext() ? askIt.next() : null;

            Object[] row = new String[11];
            row[0] = "-";
            if (bid == null) {
                for (int i = 0; i <= 4; i++) {
                    row[i] = "-";
                }
            } else {
                row[1] = bid.getPrice().toPlainString();
                row[2] = bid.getExecuted().toPlainString();
                row[3] = bid.getLeaves().toPlainString();
                row[4] = bid.getTotal().toPlainString();
            }

            row[5] = "-";

            if (ask == null) {
                for (int i = 6; i < 11; i++) {
                    row[i] = "-";
                }
            } else {
                row[6] = "-";
                row[7] = ask.getPrice().toPlainString();
                row[8] = ask.getExecuted().toPlainString();
                row[9] = ask.getLeaves().toPlainString();
                row[10] = ask.getTotal().toPlainString();
            }
            at.addRow(row);
            at.addRule();
        }
        return at.render(150);
    }

    /**
     * 表格形式展示买卖盘
     *
     * @return ascii table
     */
    public String render_bid_ask() {
        AsciiTable at = new AsciiTable();

        Iterator<Order> bidIt = bidOrders.iterator();
        Iterator<Order> askIt = askOrders.iterator();
        // 表头
        at.addRule();
        at.addRow("Id", "Side", "Type", "Price", "Quality", "LeaveQuality", "LeaveAmount", "  ", "Id", "Side", "Type", "Price", "Quality", "LeaveQuality", "LeaveAmount");
        at.addRule();
        while (bidIt.hasNext() || askIt.hasNext()) {
            Order bid = bidIt.hasNext() ? bidIt.next() : null;
            Order ask = askIt.hasNext() ? askIt.next() : null;

            Object[] row = new String[15];
            if (bid == null) {
                for (int i = 0; i < 7; i++) {
                    row[i] = "-";
                }
            } else {
                row[0] = bid.getId();
                row[1] = bid.getSide().name();
                row[2] = bid.getType().name();
                row[3] = bid.getPrice().toPlainString();

                if (bid.getPriceUpperBound().compareTo(BigDecimal.ZERO) != 0) {
                    row[3] += " (+" + bid.getPriceUpperBound().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString() + "%)";
                }

                if (bid.getPriceLowerBound().compareTo(BigDecimal.ZERO) != 0) {
                    row[3] += " (-" + bid.getPriceLowerBound().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString() + "%)";
                }

                row[4] = bid.getQuantity().toPlainString();
                row[5] = bid.getLeavesQuantity().toPlainString();
                row[6] = bid.getLeavesAmount().toPlainString();
            }

            row[7] = "  ";

            if (ask == null) {
                for (int i = 8; i < 15; i++) {
                    row[i] = "-";
                }
            } else {
                row[8] = ask.getId();
                row[9] = ask.getSide().name();
                row[10] = ask.getType().name();
                row[11] = ask.getPrice().toPlainString();

                if (ask.getPriceUpperBound().compareTo(BigDecimal.ZERO) != 0) {
                    row[11] += " (+" + ask.getPriceUpperBound().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString() + "%)";
                }

                if (ask.getPriceLowerBound().compareTo(BigDecimal.ZERO) != 0) {
                    row[11] += " (-" + ask.getPriceLowerBound().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString() + "%)";
                    ;
                }

                row[12] = ask.getQuantity().toPlainString();
                row[13] = ask.getLeavesQuantity().toPlainString();
                row[14] = ask.getLeavesAmount().toPlainString();
            }
            at.addRow(row);
            at.addRule();
        }
        return at.render(170);
    }

}
