package com.trader.exception;

/**
 * @author yjt
 * @since 2020/9/22 上午9:06
 */
public class TradeException extends RuntimeException{

    public TradeException (String errorMessage) {
        super(errorMessage);
    }
}
