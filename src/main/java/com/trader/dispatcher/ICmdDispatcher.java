package com.trader.dispatcher;

import com.trader.cmd.ICmd;

public interface ICmdDispatcher {

    /**
     * 命令分发
     *
     * @param cmd 命令
     */
    void dispatch (ICmd cmd);
}
