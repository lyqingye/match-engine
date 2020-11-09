package com.trader.core.comprator;

import com.trader.core.entity.Order;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/9/4 下午3:24
 */
public class AskComparator implements Comparator<Order> {

    /**
     * instance
     */
    private static final AskComparator INSTANCE = new AskComparator();

    @Override
    public int compare(Order o1, Order o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);

        // 订单类型排优先级
        int cmp = o1.getType().getPriority() - o2.getType().getPriority();

        if (cmp == 0) {
            // 价格低的优先
            cmp = -o1.getPrice().compareTo(o2.getPrice());

            if (cmp == 0) {
                // 同等价格下时间优先
                cmp = -(o1.getCreateDateTime().compareTo(o2.getCreateDateTime()));

                // 时间和价格同等情况下, 保留默认顺序
                if (cmp == 0) {
                    cmp = -1;
                }
            }
        }
        // invest result
        return -cmp;
    }

    public static AskComparator getInstance() {
        return INSTANCE;
    }
}
