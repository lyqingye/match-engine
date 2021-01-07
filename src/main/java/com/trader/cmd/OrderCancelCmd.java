package com.trader.cmd;

import lombok.Getter;
import lombok.Setter;

@Getter
public class OrderCancelCmd implements ICmd{
    long cmdId;
    CmdType type = CmdType.ORDER_ADD;
    long createTs = System.currentTimeMillis();
    long executeTs;
    boolean success = false;
    String failCause;
    @Setter
    String context;
    long orderId;

    public static OrderCancelCmd create(long orderId) {
        OrderCancelCmd cmd = new OrderCancelCmd();
        cmd.orderId = orderId;
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
