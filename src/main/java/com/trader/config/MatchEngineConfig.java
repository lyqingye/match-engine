package com.trader.config;

import com.trader.core.MatchHandler;
import com.trader.core.Matcher;
import com.trader.core.OrderRouter;
import com.trader.core.Scheduler;
import com.trader.market.publish.MarketPublishClient;
import lombok.Data;

/**
 * @author yjt
 * @since 2020/10/28 上午9:04
 */
@Data
public class MatchEngineConfig {
    /**
     * 订单路由 (可以为null)
     * {@link com.trader.core.support.router.GenericOrderRouter}
     */
    private OrderRouter router;

    /**
     * 调度器 (可以为null)
     * {@link com.trader.core.support.scheduler.GenericScheduler}
     */
    private Scheduler scheduler;

    /**
     * 订单匹配规则 (可以为null)
     * 默认需要添加:
     * {@link com.trader.core.matcher.market.MarketOrderMatcher}
     * {@link com.trader.core.matcher.limit.LimitOrderMatcher}
     */
    private Matcher[] matchers;

    /**
     * 撮合事件处理器数组 (可以为null)
     * 默认需要添加:
     * {@link com.trader.core.matcher.limit.InMemoryLimitMatchHandler}
     * {@link com.trader.core.matcher.market.InMemoryMarketMatchHandler}
     */
    private MatchHandler handler;

    /**
     * 全局下单队列大小 2的n次方
     * 如果你的系统需要一秒钟吃单100w
     * 那么相应的值就应该是 1 << 20 = 1048576
     */
    private int sizeOfOrderQueue = 1 << 16;

    /**
     * 撮合引擎总核心数
     * 如果为 4核机器推荐核心数为 1
     * 如果为 8核机器推荐核心数为 2
     * ...
     */
    private int numberOfCores = 1;

    /**
     * 每一个核心数的命令缓冲队列大小, 当有数据到达下单队列时, 调度器会将所有订单封装成命令
     * 并且放入此缓冲队列, 每个核心都有自己独立的缓冲队列.
     * 当你的系统只有一颗核心, 那么缓冲队列大小应该和 {@link #numberOfCores} 一致
     */
    private int sizeOfCoreCmdBuffer = 1 << 16;

    /**
     * 推送数据合并缓冲区大小, 在添加订单或者撮合订单或取消订单后或激活止盈止损订单时
     * 都会引起盘口变动, 可能在一秒钟内会有n条相同的盘口信息被推送, 这将很浪费io资源
     * 所以该队列的作用是将一个周期 {@link #publishDataCompressCycle} 内产生的数据合并成一条
     */
    private int sizeOfPublishDataRingBuffer = 1 << 16;

    /**
     * 推荐数据合并周期, 单位为毫秒
     */
    private long publishDataCompressCycle = 0;

    /**
     * 撮合结果队列大小, 在订单撮合成功后需要推送消息此队列用作缓冲
     * 当你希望你的系统每秒能承受1w笔订单的成交的时候该值应该设置为 1 << 14 = 16384
     */
    private int sizeOfTradeResultQueue = 1 << 16;

    /**
     * 消息推送客户端, 默认为 tcp进行推送
     * {@link com.trader.market.publish.TcpMarketPublishClient}
     */
    private MarketPublishClient marketPublishClient;

    /**
     * 消息推送域名, 默认为 localhost
     */
    private String marketPublishClientHost = "localhost";

    /**
     * 消息推送端口, 默认为 8888
     */
    private int marketPublishClientPort = 8888;

    /**
     * websocket 配置客户端域名
     */
    private String websocketConfigClientHost = "localhost";

    /**
     * websocket 配置客户端端口
     */
    private int websocketConfigClientPort = 8087;
}
