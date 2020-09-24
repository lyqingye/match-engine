package com.trader.builder;

import com.trader.def.OrderSide;
import com.trader.entity.Order;

/**
 * @author yjt
 * @since 2020/9/18 下午6:05
 */
public class LimitOrderBuilder {
    private Order order;

    public static LimitOrderBuilder buy() {
        LimitOrderBuilder builder = new LimitOrderBuilder();
        builder.order = new Order();
        builder.order.setSide(OrderSide.BUY);
        return builder;
    }

    public static LimitOrderBuilder sell() {
        LimitOrderBuilder builder = new LimitOrderBuilder();
        builder.order = new Order();
        builder.order.setSide(OrderSide.SELL);
        return builder;
    }


}
