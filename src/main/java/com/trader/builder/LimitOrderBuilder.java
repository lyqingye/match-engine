package com.trader.builder;

import com.trader.def.OrderSide;
import com.trader.entity.Order;

import java.util.Date;

/**
 * @author yjt
 * @since 2020/9/18 下午6:05
 */
public class LimitOrderBuilder {

    public static LimitOrderBuilder buy(String symbolId) {
        BuyLimitOrderBuilder builder = new BuyLimitOrderBuilder();
        builder.order.setSymbol(symbolId);
        return builder;
    }




    public static class BuyLimitOrderBuilder extends LimitOrderBuilder {
        private Order order;

        public BuyLimitOrderBuilder () {
            order = new Order();
            order.setSide(OrderSide.BUY);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

//        public BuyLimitOrderBuilder
    }
}
