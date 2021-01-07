package com.trader.cmd;

import com.trader.core.entity.Order;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Data
@Getter
public class OrderAddCmd implements ICmd{
    long cmdId;
    CmdType type = CmdType.ORDER_ADD;
    long createTs = System.currentTimeMillis();
    long executeTs;
    boolean success = false;
    String failCause;
    @Setter
    String context;
    Order order;

    public static OrderAddCmd create(Order order) {
        OrderAddCmd cmd = new OrderAddCmd();
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
