package com.trader.market.thrid;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author yjt
 * @since 2020/10/10 下午6:51
 */
public class MarketPublishClient {
    /**
     * 域名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * channel
     */
    private Channel channel;

    public void start (String host,int port) throws Exception{
        // 处理阻塞业务
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) {
                    // 解码
                    channel.pipeline().addLast(StringDecoder.class.getName(), new StringDecoder(CharsetUtil.UTF_8));
                    // 编码
                    channel.pipeline().addLast(StringEncoder.class.getName(), new StringEncoder(CharsetUtil.UTF_8));

                    // 消息处理
                    channel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx,
                                                    String msg) throws Exception {
                            System.out.println(msg);
                        }
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            final EventLoop loop = ctx.channel().eventLoop();
                            loop.schedule(() -> {
                                try {
                                    start(host, port);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }, 5L, TimeUnit.SECONDS);
                        }
                    });
                }
            });


            // 绑定端口
            ChannelFuture future = bootstrap.connect(host, port).sync();

            this.channel = future.channel();
            this.channel.closeFuture().sync();
        }  finally {
            workerGroup.shutdownGracefully();
        }
    }

    public boolean isOpen() {
        return this.channel.isOpen();
    }

    public Channel getChannel () {
        if (this.channel == null) {
            return null;
        }
        if (this.channel.isOpen()) {
            return this.channel;
        }
        return null;
    }
}
