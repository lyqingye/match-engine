package com.trader.market.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/9/22 上午10:50
 */
@Data
public class MarketDepthInfo {

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private BigDecimal executed;

    /**
     * 剩余量
     */
    private BigDecimal leaves;

    /**
     * 总量
     */
    private BigDecimal total;

    @Override
    public MarketDepthInfo clone(){
        MarketDepthInfo c = new MarketDepthInfo();
        c.setExecuted(this.executed);
        c.setLeaves(this.leaves);
        c.setPrice(this.price);
        c.setTotal(this.total);
        return c;
    }

    public void add (MarketDepthInfo a) {
        this.setExecuted(this.getExecuted().add(a.getExecuted()));
        this.setTotal(this.getTotal().add(a.getTotal()));
        this.setLeaves(this.getLeaves().add(a.getLeaves()));
    }

    public static MarketDepthInfo add (MarketDepthInfo o1,MarketDepthInfo o2) {
        MarketDepthInfo sum = new MarketDepthInfo();
        sum.setPrice(o1.getPrice());
        sum.setExecuted(o1.getExecuted().add(o2.getExecuted()));
        sum.setTotal(o1.getTotal().add(o2.getTotal()));
        sum.setLeaves(o1.getLeaves().add(o2.getLeaves()));
        return sum;
    }

    public static MarketDepthInfo empty () {
        MarketDepthInfo e = new MarketDepthInfo();
        e.setExecuted(BigDecimal.ZERO);
        e.setTotal(BigDecimal.ZERO);
        e.setPrice(BigDecimal.ZERO);
        e.setLeaves(BigDecimal.ZERO);
        return e;
    }

    public int compareTo (MarketDepthInfo target) {
        return this.getPrice().compareTo(target.getPrice());
    }

    public int reverseCompare (MarketDepthInfo target) {
        return target.getPrice().compareTo(this.getPrice());
    }

}
