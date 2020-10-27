package com.trader.event;

import com.trader.MatchHandler;
import com.trader.entity.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author yjt
 * @since 2020/10/23 上午10:25
 */
public class MatchEventHandlerRegistry {
    /**
     * 处理器列表
     */
    private List<MatchHandler> handlers = new ArrayList<>(16);

    /**
     * 添加一个事件处理器
     *
     * @param h
     *         {@link MatchHandler}
     */
    public void regHandler(MatchHandler h) {
        Objects.requireNonNull(h, "handler is null");
        this.handlers.add(h);

        // 排序
        this.handlers.sort(Comparator.comparing(MatchHandler::getPriority).reversed());
    }

    /**
     * 获取所有处理器
     *
     * @return 处理器列表
     */
    protected List<MatchHandler> handlers() {
        return this.handlers;
    }

    /**
     * 执行处理器,当其中任意一个处理失败的时, 其后续的处理器将不会继续执行
     *
     * @param f
     *         handler 消费者
     *
     * @throws Exception
     */
    protected void executeHandler(Consumer<MatchHandler> f) {
        for (int i = 0; i < this.handlers.size(); i++) {
            MatchHandler h = this.handlers.get(i);
            f.accept(h);
        }
    }

    /**
     * 执行订单取消移除事件
     *
     * @param order
     *         已经移除的订单
     */
    protected void executeOrderCancel(Order order) {
        this.executeHandler((h) -> {
            h.onOrderCancel(order);
        });
    }

}
