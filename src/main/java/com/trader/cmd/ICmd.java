package com.trader.cmd;

/**
 * 命令接口
 */
public interface ICmd {
    /**
     * @return 命令Id
     */
    long getCmdId();

    /**
     * @return 命令类型
     */
    CmdType getType ();

    /**
     * @return 命令创建时间
     */
    long getCreateTs();

    /**
     * @return 命令执行时间
     */
    long getExecuteTs ();

    /**
     * @return 命令执行是否成功
     */
    boolean isSuccess();

    /**
     * @return 命令执行失败原因
     */
    String getFailCause();

    /**
     * @return 用于跟踪上下文的数据
     */
    String getContext();
}
