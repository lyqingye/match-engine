package com.trader.handler;

import com.trader.MatchHandler;
import com.trader.entity.Order;
import com.trader.matcher.TradeResult;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/17 下午7:57
 */
public class ExampleLoggerHandler implements MatchHandler {

    @Override
    public void onExecuteOrder(Order order,
                               Order opponentOrder, TradeResult ts) throws Exception {
        BigDecimal quantity = ts.getQuantity();



        if (!opponentOrder.isMarketOrder()) {

            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s" ,
                                             order.getId(),
                                             order.isBuy() ? "买入" : "卖出",
                                             quantity.toPlainString(),
                                             ts.getExecutePrice().toPlainString(),
                                             order.getLeavesQuantity().toPlainString(),
                                             order.getLeavesAmount().toPlainString()));


            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
                                             opponentOrder.getId(),
                                             opponentOrder.isBuy() ? "买入" : "卖出",
                                             quantity.toPlainString(),
                                             ts.getOpponentExecutePrice().toPlainString(),
                                             opponentOrder.getLeavesQuantity().toPlainString(),
                                             opponentOrder.getLeavesAmount().toPlainString()));
        }else {

            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
                                             order.getId(),
                                             order.isBuy() ? "买入" : "卖出",
                                             quantity.toPlainString(),
                                             ts.getExecutePrice().toPlainString(),
                                             order.getLeavesQuantity().toPlainString(),
                                             order.getLeavesAmount().toPlainString()));


            System.out.println(String.format("订单: %s %s 成交数量: %s 成交价格: %s 剩余数量: %s 剩余金额: %s",
                                             opponentOrder.getId(),
                                             opponentOrder.isBuy() ? "买入" : "卖出",
                                             quantity.toPlainString(),
                                             ts.getOpponentExecutePrice().toPlainString(),
                                             opponentOrder.getLeavesQuantity().toPlainString(),
                                             opponentOrder.getLeavesAmount().toPlainString()));
        }


        System.out.println("--------------------------------------------------------");
    }

}
