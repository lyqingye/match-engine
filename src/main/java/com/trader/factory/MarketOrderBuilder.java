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
public class MarketOrderBuilder {

    public BuyMarketOrderBuilder buy(String uid, String coinId, String currencyId) {
        BuyMarketOrderBuilder builder = new BuyMarketOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.MARKET);
        return builder;
    }

    public SellMarketOrderBuilder sell(String uid, String coinId, String currencyId) {
        SellMarketOrderBuilder builder = new SellMarketOrderBuilder();
        builder.order.setCoinId(coinId);
        builder.order.setCurrencyId(currencyId);
        builder.order.setUid(uid);
        builder.order.setId(SnowflakeIdWorker.nextId());
        builder.order.setType(OrderType.MARKET);
        return builder;
    }

    public static class SellMarketOrderBuilder  {
        private Order order;

        public SellMarketOrderBuilder () {
            order = new Order();
            order.setSide(OrderSide.SELL);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public SellMarketOrderBuilder quantity (BigDecimal quantity) {
            order.setQuantity(quantity);
            order.setLeavesQuantity(quantity);
            return this;
        }

        public SellMarketOrderBuilder AOK () {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public SellMarketOrderBuilder GTC () {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public SellMarketOrderBuilder IOC () {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public SellMarketOrderBuilder FOK () {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public Order build () {
            return this.order;
        }
    }

    public static class BuyMarketOrderBuilder{
        private Order order;

        public BuyMarketOrderBuilder () {
            order = new Order();
            order.setSide(OrderSide.BUY);
            order.setCreateDateTime(new Date());
            order.setVersion(0);
        }

        public BuyMarketOrderBuilder spent (BigDecimal totalAmount) {
            order.setTotalAmount(totalAmount);
            order.setLeavesAmount(totalAmount);
            return this;
        }

        public BuyMarketOrderBuilder AOK () {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public BuyMarketOrderBuilder GTC () {
            order.setTimeInForce(OrderTimeInForce.GTC);
            return this;
        }

        public BuyMarketOrderBuilder IOC () {
            order.setTimeInForce(OrderTimeInForce.IOC);
            return this;
        }

        public BuyMarketOrderBuilder FOK () {
            order.setTimeInForce(OrderTimeInForce.FOK);
            return this;
        }

        public Order build () {
            return this.order;
        }
    }
}
