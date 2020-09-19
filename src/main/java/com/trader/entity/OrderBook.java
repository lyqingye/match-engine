package com.trader.entity;

import com.trader.cmp.AskComparator;
import com.trader.cmp.BidComparator;
import de.vandermeer.asciitable.AsciiTable;
import lombok.Data;

import java.util.Iterator;
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
     * 该账本所支持的货币
     */
    private Currency currency;

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

    public void removeOrder(Order o) {
        TreeSet<Order> orders = null;
        switch (o.getType()) {
            /**
             * 市价单和限价单
             */
            case MARKET:
            case LIMIT: {
                if (o.isBuy()) {
                    orders = bidOrders;
                } else {
                    orders = askOrders;
                }
                break;
            }

            case STOP: {
                /**
                 * 止盈止损单
                 */
                if (o.isBuy()) {
                    orders = buyStopOrders;
                } else {
                    orders = sellStopOrders;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid order type");
            }
        }

        orders.removeIf(v -> v.getId().equals(o.getId()));
    }

    public Order getBestBid() {
        return bidOrders.first();
    }

    public Order getBestAsk() {
        return askOrders.first();
    }

    public Order getBestSellStop() {
        return sellStopOrders.first();
    }

    public Order getBestBuyStop() {
        return buyStopOrders.first();
    }


    //
    // DEBUG ONLY
    //
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

            String[] row = new String[15];
            if (bid == null) {
                for (int i = 0; i < 7; i++) {
                    row[i] = "-";
                }
            } else {
                row[0] = bid.getId();
                row[1] = bid.getSide().name();
                row[2] = bid.getType().name();
                row[3] = bid.getPrice().toPlainString();
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
