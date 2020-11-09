package com.trader.core.factory;

import com.trader.core.def.OrderSide;
import com.trader.core.def.OrderTimeInForce;
import com.trader.core.def.OrderType;
import com.trader.core.entity.Order;
import com.trader.utils.SnowflakeIdWorker;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yjt
 * @since 2020/9/18 下午6:05
 */
public class LimitOrderBuilder {

    public BuyLimitOrderBuilder buy(String uid, String coinId, String currencyId) {
        BuyLimitOrderBuilder builder = new BuyLimitOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.LIMIT);
        return builder;
    }

    public SellLimitOrderBuilder sell(String uid, String coinId, String currencyId) {
        SellLimitOrderBuilder builder = new SellLimitOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.LIMIT);
        return builder;
    }

    public static class SellLimitOrderBuilder {
        private Order order;

        public SellLimitOrderBuilder() {
            order = new Order();
            order.setSide(OrderSide.SELL);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public SellLimitOrderBuilder quantity(BigDecimal quantity) {
            order.setQuantity(quantity);
            order.setLeavesQuantity(quantity);
            return this;
        }

        public SellLimitOrderBuilder withUnitPriceOf(BigDecimal unitPrice) {
            order.setPrice(unitPrice);
            return this;
        }

        public SellLimitOrderBuilder withUnitPriceCap(BigDecimal priceLower) {
            order.setPriceLowerBound(priceLower);
            return this;
        }

        public SellLimitOrderBuilder AOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public SellLimitOrderBuilder GTC() {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public SellLimitOrderBuilder IOC() {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public SellLimitOrderBuilder FOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public Order build() {
            return this.order;
        }
    }

    public static class BuyLimitOrderBuilder {
        private Order order;

        public BuyLimitOrderBuilder() {
            order = new Order();
            order.setSide(OrderSide.BUY);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public BuyLimitOrderBuilder spent(BigDecimal totalAmount) {
            order.setTotalAmount(totalAmount);
            order.setLeavesAmount(totalAmount);
            return this;
        }

        public BuyLimitOrderBuilder withUnitPriceOf(BigDecimal unitPrice) {
            order.setPrice(unitPrice);
            return this;
        }

        public BuyLimitOrderBuilder quantity(BigDecimal quantity) {
            order.setQuantity(quantity);
            return this;
        }

        public BuyLimitOrderBuilder withUnitPriceCap(BigDecimal priceUpper) {
            order.setPriceUpperBound(priceUpper);
            return this;
        }

        public BuyLimitOrderBuilder AOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public BuyLimitOrderBuilder GTC() {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public BuyLimitOrderBuilder IOC() {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public BuyLimitOrderBuilder FOK() {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public Order build() {
            return this.order;
        }
    }
}
