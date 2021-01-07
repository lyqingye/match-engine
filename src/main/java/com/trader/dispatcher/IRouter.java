package com.trader.dispatcher;

import com.trader.core.entity.Order;
import com.trader.core.entity.OrderBook;

public interface IRouter {

    OrderBook toMatchBook (Order order);
}
