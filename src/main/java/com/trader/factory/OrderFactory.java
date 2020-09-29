package com.trader.factory;

/**
 * @author ex
 */
public final class OrderFactory {

    public static LimitOrderBuilder limit () {
        return new LimitOrderBuilder();
    }

    public static MarketOrderBuilder market () {return new MarketOrderBuilder();}

    public static StopOrderBuilder stop () {
        return new StopOrderBuilder();
    }
}
