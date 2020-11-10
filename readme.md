### 撮合引擎2.0

#### 1. 核心功能
+ 限价订单
+ 市价订单
+ 止盈止损订单
+ 消息推送及三方价格同步
+ 订单路由, 可针对特殊订单另开虚拟盘进行独立撮合
+ 多核心多买卖盘并行撮合

#### 2. 项目结构说明
>>> 
    ├── config         撮合引擎配置
    ├── core           撮合引擎核心
    │   ├── comprator  订单排序比较器
    │   ├── context    撮合引擎上下文
    │   ├── def        常量定义
    │   ├── entity     实体对象定义
    │   ├── event      事件处理管理器
    │   ├── exception  撮合异常
    │   ├── factory    订单工厂
    │   ├── handler    事件处理器
    │   ├── matcher    撮合匹配器
    │   └── support    
    |       ├── processor 买卖盘处理核心
    |       ├── router    订单路由
    |       ├── scheduler 订单调度器
    |
    ├── market          市场行情管理器
    └── utils
        ├── buffer      推送数据合并队列
        ├── disruptor   无锁队列
        ├── messages    快速序列化/反序列化推送消息
        
#### 3. 工作流程图
> https://www.processon.com/diagraming/5f01f2c3f346fb1ae5933f84

        
#### 4. 快速入门
```gradle
    // gradle 引入
    api "com.trader:match-engine:2.0.7"
```
```java
    MatchEngineConfig config = new MatchEngineConfig();
    // 设置消息推送配置服务的ip
    config.setWebsocketConfigClientHost("119.23.49.169");
    // 设置消息推送服务的ip
    config.setMarketPublishClientHost("119.23.49.169");
    config.setHandler(new MatchHandler() {
        /**
         * 撮合订单事件
         *
         * @param order
         *         订单
         * @param opponentOrder
         *         对手订单
         * @param ts
         *         撮合结果
         *
         * @throws Exception
         */
        @Override
        public void onExecuteOrder(Order order,
                                   Order opponentOrder,
                                   TradeResult ts) throws Exception {
            // 持久化工作
            entrustFlowService.trader(order, opponentOrder, ts);
        }
    });
    MatchEngine engine = MatchEngine.newEngine(config);

    // 开启立即撮合
    engine.enableMatching();
    return engine;
```        
        
#### 5. 撮合引擎配置说明
+ OrderRouter
    > 订单路由, 默认使用 `com.trader.core.support.router.GenericOrderRouter`
    用于划分哪些订单放在哪个撮合买卖盘
+ Scheduler
    > 订单调度器, 默认使用 `com.trader.core.support.scheduler.GenericScheduler`
    该调度器基于线程进行调度, 每一条线程一个撮合核心, 一个撮合核心可以撮合一个或者多个买卖盘
+ Matcher          
    > 订单匹配规则, 默认使用
    > + com.trader.core.matcher.market.MarketOrderMatcher 市价匹配规则
    > + com.trader.core.matcher.limit.LimitOrderMatcher 限价匹配规则
+ MatchHandler
    > 撮合事件处理器, 用于监听所有撮合事件
+ sizeOfOrderQueue
    > 下单队列的大小, 队列大小必须为 2^n次方, 如果你的系统需要一秒钟吃单100w 那么相应的值就应该是 1 << 20 = 1048576
    默认值： 1 << 16
+ numberOfCores
    > 撮合引擎核心数, 一个核心可以撮合多个买卖盘, 多个核心可以并行撮合
    + 如果为 4核机器推荐核心数为 1
    + 如果为 8核机器推荐核心数为 2
+ sizeOfCoreCmdBuffer
    > 每一个核心对应的命令缓冲队列大小, 当有数据到达下单队列时, 调度器会将所有订单封装成命令
    并且放入此缓冲队列, 每个核心都有自己独立的缓冲队列. 如果你有多个交易对需要进行撮合, 并且你开启了
    并行撮合, 那该值 = 平均每个核心所对应的吃单量
+ sizeOfPublishDataRingBuffer
    > 消息推送数据缓冲队列大小
+ publishDataCompressCycle
    > 推送数据合并周期 单位为毫秒, 假设值为 1000, 那么系统会将一秒钟产生的推送数据进行合并
    可以减少带宽压力
+ marketPublishClientHost
    > 消息推送服务host
+ marketPublishClientPort
    > 消息推送服务端口
+ websocketConfigClientHost
    > 消息推送配置客户端host
+ websocketConfigClientPort
    > 消息推送配置客户端端口                              
                                                                                  