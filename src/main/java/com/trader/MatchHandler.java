package com.trader;

import com.trader.entity.Order;
import com.trader.entity.OrderBook;
import com.trader.entity.Product;

import java.math.BigDecimal;

/**
 * 撮合引擎事件处理器
 *
 * @author yjt
 * @since 2020/9/1 上午10:25
 */
public interface MatchHandler {

    /**
     * 产品添加事件处理器
     * @param product
     * @throws Exception
     */
    void onAddProduct (Product product) throws Exception;

    /**
     * 产品事件处理器
     * @param product
     * @throws Exception
     */
    void onDelProduct (Product product) throws Exception;

    /**
     * 委托账本添加事件
     * @param book
     * @throws Exception
     */
    void onAddOrderBook (OrderBook book) throws Exception;

    /**
     * 委托账本删除事件
     * @param book
     * @throws Exception
     */
    void onDelOrderBook (OrderBook book) throws Exception;

    /**
     * 添加订单事件
     * @param newOrder
     * @throws Exception
     */
    void onAddOrder (Order newOrder) throws Exception;

    /**
     * 更新订单事件
     * @param order
     * @throws Exception
     */
    void onUpdateOrder (Order order) throws Exception;

    /**
     * 删除订单事件
     * @param order
     * @throws Exception
     */
    void onDelOrder (Order order) throws Exception;

    /**
     * 撮合订单事件
     * @param order
     * @param price
     * @param quantity
     * @throws Exception
     */
    void onExecuteOrder (Order order, BigDecimal price,BigDecimal quantity) throws Exception;
}
