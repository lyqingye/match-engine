package com.trader.core.event;

/**
 * 可分区的
 *
 * @author lyqingye
 */
public interface Separable {

    /**
     * 分区ID
     *
     * @return 分区ID
     */
    String partitionId ();
}
