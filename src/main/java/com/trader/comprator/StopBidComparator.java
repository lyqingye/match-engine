package com.trader.comprator;

import com.trader.entity.Order;

import java.util.Comparator;

/**
 * @author yjt
 * @since 2020/9/24 下午2:17
 */
public class StopBidComparator implements Comparator<Order> {

    /**
     * instance
     */
    private static final StopBidComparator INSTANCE = new StopBidComparator();

    private static final Comparator<Order> DEFAULT;

    static {
        DEFAULT = Comparator.comparing(Order::getTriggerPrice).reversed()
                            .thenComparing(Order::getCreateDateTime);
    }

    @Override
    public int compare(Order order, Order t1) {
        return DEFAULT.compare(order, t1);
    }

    public static StopBidComparator getInstance() {
        return INSTANCE;
    }
}
