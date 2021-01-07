package com.trader.dispatcher.impl;

import com.trader.cmd.*;
import com.trader.dispatcher.ICmdDispatcher;

public class SimpleCmdDispatcher implements ICmdDispatcher {

    @Override
    public void dispatch(ICmd cmd) {
        if (cmd instanceof OrderAddCmd) {
            return;
        }

        if (cmd instanceof OrderCancelCmd) {
            return;
        }

        if (cmd instanceof OrderActiveCmd) {
            return;
        }

        if (cmd instanceof PriceChangeCmd) {
        }
    }
}
