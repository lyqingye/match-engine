package com.trader.matcher;

import com.trader.Matcher;
import com.trader.entity.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/10/23 上午11:37
 */
public class MatcherManager {
    /**
     * 撮合匹配器
     */
    private List<Matcher> matchers = new ArrayList<>(16);

    /**
     * 添加一个匹配器
     *
     * @param matcher
     *         匹配器
     */
    public void addMatcher(Matcher matcher) {
        this.matchers.add(Objects.requireNonNull(matcher));
    }

    /**
     * 根据订单搜索合适的匹配器, 如果没有找到合适的匹配器那么则返回 {@code null}
     *
     * @param order
     *         订单
     * @param opponentOrder
     *         对手订单
     *
     * @return 匹配器
     */
    public Matcher lookupMatcher(Order order, Order opponentOrder) {
        return this.matchers.stream()
                            .filter(matcher -> matcher.isSupport(order, opponentOrder))
                            .findFirst()
                            .orElse(null);
    }
}
