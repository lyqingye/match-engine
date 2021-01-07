package com.trader.cmd;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PriceChangeCmd implements ICmd {
    long cmdId;
    CmdType type = CmdType.ORDER_ADD;
    long createTs = System.currentTimeMillis();
    long executeTs;
    boolean success = false;
    String failCause;
    @Setter
    String context;
    String symbol;
    double newPrice;

    public static PriceChangeCmd create(String symbol,double newPrice) {
        PriceChangeCmd cmd = new PriceChangeCmd();
        cmd.symbol = symbol;
        cmd.newPrice = newPrice;
        return cmd;
    }

    public void success() {
        this.success = true;
        this.executeTs = System.currentTimeMillis();
    }

    public void fail(String failCause) {
        this.failCause = failCause;
        this.executeTs = System.currentTimeMillis();
    }
}
