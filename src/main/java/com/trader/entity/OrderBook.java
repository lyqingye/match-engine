package com.trader.entity;

import com.trader.cmp.AskComparator;
import com.trader.cmp.BidComparator;
import lombok.Data;

import java.util.Objects;
import java.util.TreeSet;

/**
 * @author yjt
 * @since 2020/9/1 上午10:12
 */
@Data
public class OrderBook {

    /**
     * 该账本所记录的商品
     */
    private Product product;

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
    private TreeSet<Order> buyStopOrders = new TreeSet<>(AskComparator.getInstance());

    /**
     * 止盈止损 (卖出)
     */
    private TreeSet<Order> sellStopOrders = new TreeSet<>(BidComparator.getInstance());

    /**
     * 增加订单
     * @param o
     */
    public void addOrder (Order o) {
        Objects.requireNonNull(o, "order is null");

        switch (o.getType()) {
            /**
             * 市价单和限价单
             */
            case MARKET:
            case STOP: {
                if (o.isBuy()) {
                    bidOrders.add(o);
                }else {
                    askOrders.add(o);
                }
                break;
            }

            case LIMIT: {
                /**
                 * 止盈止损单
                 */
                if (o.isBuy()) {
                    buyStopOrders.add(o);
                }else {
                    sellStopOrders.add(o);
                }
                break;
            }

            default: {
                throw new IllegalArgumentException("invalid order type");
            }
        }
    }

    public Order getBestBid () {
        return bidOrders.first();
    }

    public Order getBestAsk () {
        return askOrders.first();
    }

    public Order getBestSellStop () {
        return sellStopOrders.first();
    }

    public Order getBestBuyStop () {
        return buyStopOrders.first();
    }

}
