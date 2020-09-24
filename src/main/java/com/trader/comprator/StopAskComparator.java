package com.trader.comprator;

import com.trader.entity.Order;

import java.util.Comparator;

/**
 * 止盈止损卖单比较器
 *
 * @author yjt
 * @since 2020/9/24 下午12:39
 */
public class StopAskComparator implements Comparator<Order> {

    /**
     * instance
     */
    private static final StopAskComparator INSTANCE = new StopAskComparator();

    private static final Comparator<Order> DEFAULT;

    static {
        DEFAULT = Comparator.comparing(Order::getTriggerPrice)
                            .thenComparing(Order::getCreateDateTime);
    }

    @Override
    public int compare(Order order, Order t1) {
        return DEFAULT.compare(order, t1);
    }

    public static StopAskComparator getInstance() {
        return INSTANCE;
    }
}
