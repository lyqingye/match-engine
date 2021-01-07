package com.trader.cmd;

import com.trader.core.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class OrderActiveCmd implements ICmd{
    long cmdId;
    CmdType type = CmdType.ORDER_ADD;
    long createTs = System.currentTimeMillis();
    long executeTs;
    boolean success = false;
    String failCause;
    @Setter
    String context;
    Order order;

    public static OrderActiveCmd create(Order order) {
        OrderActiveCmd cmd = new OrderActiveCmd();
        cmd.order = Objects.requireNonNull(order);
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
