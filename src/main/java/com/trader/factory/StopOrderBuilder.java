package com.trader.factory;

import com.trader.def.OrderSide;
import com.trader.def.OrderTimeInForce;
import com.trader.def.OrderType;
import com.trader.entity.Order;
import com.trader.utils.SnowflakeIdWorker;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ex
 */
public class StopOrderBuilder {
    public BuyStopOrderBuilder buy(String uid, String coinId, String currencyId) {
        BuyStopOrderBuilder builder = new BuyStopOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.STOP);
        return builder;
    }

    public SellStopOrderBuilder sell(String uid, String coinId, String currencyId) {
        SellStopOrderBuilder builder = new SellStopOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.STOP);
        return builder;
    }

    public static class SellStopOrderBuilder {
        private Order order;

        public SellStopOrderBuilder() {
            order = new Order();
            order.setSide(OrderSide.SELL);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public SellStopOrderBuilder quantity(BigDecimal quantity) {
            order.setQuantity(quantity);
            order.setLeavesQuantity(quantity);
            return this;
        }

        public SellStopOrderBuilder withUnitPriceOf(BigDecimal unitPrice) {
            order.setPrice(unitPrice);
            return this;
        }

        public SellStopOrderBuilder withUnitPriceCap(BigDecimal priceLower) {
            order.setPriceLowerBound(priceLower);
            return this;
        }

        public SellStopOrderBuilder AOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public SellStopOrderBuilder GTC() {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public SellStopOrderBuilder IOC() {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public SellStopOrderBuilder FOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public SellStopOrderBuilder triggerByUnitPrice(BigDecimal triggerPrice) {
            order.setTriggerPrice(triggerPrice);
            return this;
        }

        public Order build() {
            return this.order;
        }
    }

    public static class BuyStopOrderBuilder {
        private Order order;

        public BuyStopOrderBuilder() {
            order = new Order();
            order.setSide(OrderSide.BUY);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public BuyStopOrderBuilder spent(BigDecimal totalAmount) {
            order.setTotalAmount(totalAmount);
            order.setLeavesAmount(totalAmount);
            return this;
        }

        public BuyStopOrderBuilder withUnitPriceOf(BigDecimal unitPrice) {
            order.setPrice(unitPrice);
            return this;
        }

        public BuyStopOrderBuilder withUnitPriceCap(BigDecimal priceUpper) {
            order.setPriceUpperBound(priceUpper);
            return this;
        }

        public BuyStopOrderBuilder AOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public BuyStopOrderBuilder GTC() {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public BuyStopOrderBuilder IOC() {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public BuyStopOrderBuilder FOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public BuyStopOrderBuilder triggerByUnitPrice(BigDecimal triggerPrice) {
            order.setTriggerPrice(triggerPrice);
            return this;
        }

        public Order build() {
            return this.order;
        }
    }
}
