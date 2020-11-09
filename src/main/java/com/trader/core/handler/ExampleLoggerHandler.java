package com.trader.core.handler;

import com.trader.core.MatchHandler;
import com.trader.core.entity.Order;
import com.trader.core.matcher.TradeResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yjt
 * @since 2020/9/17 下午7:57
 */
public class ExampleLoggerHandler implements MatchHandler {
    AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void onAddOrder(Order newOrder) throws Exception {
//        System.out.println(String.format("[Match Log]: [Add Order] id: %s [%s] [%s] ",
//                                         newOrder.getId(), newOrder.getSide().name(), newOrder.getType().name()));
    }

    @Override
    public void onOrderCancel(Order removed) {

    }

    @Override
    public void onActiveStopOrder(Order stopOrder) throws Exception {

    }

    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, TradeResult ts) throws Exception {

//        BigDecimal quantity = ts.getQuantity();
//
//
//        if (!opponentOrder.isMarketOrder()) {
//
//            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
//                                             order.getId(),
//                                             order.isBuy() ? "买入" : "卖出",
//                                             quantity.toPlainString(),
//                                             ts.getExecutePrice().toPlainString(),
//                                             order.getLeavesQuantity().toPlainString(),
//                                             order.getLeavesAmount().toPlainString()));
//
//
//            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
//                                             opponentOrder.getId(),
//                                             opponentOrder.isBuy() ? "买入" : "卖出",
//                                             quantity.toPlainString(),
//                                             ts.getOpponentExecutePrice().toPlainString(),
//                                             opponentOrder.getLeavesQuantity().toPlainString(),
//                                             opponentOrder.getLeavesAmount().toPlainString()));
//        } else {
//
//            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
//                                             order.getId(),
//                                             order.isBuy() ? "买入" : "卖出",
//                                             quantity.toPlainString(),
//                                             ts.getExecutePrice().toPlainString(),
//                                             order.getLeavesQuantity().toPlainString(),
//                                             order.getLeavesAmount().toPlainString()));
//
//
//            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
//                                             opponentOrder.getId(),
//                                             opponentOrder.isBuy() ? "买入" : "卖出",
//                                             quantity.toPlainString(),
//                                             ts.getOpponentExecutePrice().toPlainString(),
//                                             opponentOrder.getLeavesQuantity().toPlainString(),
//                                             opponentOrder.getLeavesAmount().toPlainString()));

//        }
        if (counter.get() == 0) {
            System.out.println("startxxx: " + System.currentTimeMillis());
        }
        if (counter.incrementAndGet() % 10000 == 0) {
            System.out.println(counter.get());
            System.out.println(System.currentTimeMillis());
        }
    }

}