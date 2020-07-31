package com.yuhaowin.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;

/**
 * http 服务器
 */
public class HttpServer {
    int port ;
    public HttpServer(int port){
        this.port = port;
    }

    public void start() throws Exception{
        /**
         * ServerBootStrap是 Netty服务端启动配置类
         * 负责初始化netty服务器，并且开始监听指定端口的socket请求。
         */
        ServerBootstrap bootstrap = new ServerBootstrap();

        /**
         * // 如果不指定线程大小，默认为  cpu 核心数 * 2
         * @see MultithreadEventLoopGroup#DEFAULT_EVENT_LOOP_THREADS
         * 在 parentGroup 中，一个 EventLoop 即可监听一个端口，该 demo 程序只需要监听一个
         * 端口 固这个我设置为 1 个线程。
         */
        EventLoopGroup parentGroup = new NioEventLoopGroup(1);

        EventLoopGroup childGroup = new NioEventLoopGroup();

        /**
         * 绑定线程池 这是设置的是 主从多线程
         * parentGroup 主线程组
         * childGroup  IO 操作线程组
         *
         * 可以将主线程池和工作线程池设置为同一个线程组。
         */
        bootstrap.group(parentGroup,childGroup)
                /**
                 * 设置通讯模式，调用的是实现io.netty.channel.Channel接口的类。
                 * 如：NioSocketChannel、NioServerSocketChannel，服务端一般可以选NioServerSocketChannel。
                 * ServerSocketChannel实现类：
                 * - NioServerSocketChannel
                 * - OioServerSocketChannel
                 * - EpollServerSocketChannel
                 * - KQueueServerSocketChannel
                 */
                .channel(NioServerSocketChannel.class)
                /**
                 * 通过handler添加的handlers是对bossGroup线程组起作用
                 * 发生在初始化阶段
                 * 设置到 abstractBootstrap 中 对 NioServerSocketChannel 产生作用
                 */
                .handler(new LoggingHandler(LogLevel.DEBUG))
                /**
                 * 通过childHandler添加的handlers是对workerGroup线程组起作用
                 * 发生在客户端建立 socket 连接后
                 * 设置到 serverBootstrap 中 对 NioSocketChannel 产生作用
                 */
                .childHandler(new HttpServerInitializer());

        /**
         * ChannelFuture 用来保存Channel异步操作的结果。
         */
        ChannelFuture channelFuture = bootstrap
                // 在绑定端口时会 调用 init 方法。
                .bind(new InetSocketAddress(port))
                .sync();//同步等待绑定结束

        System.out.println(" server start up on port : " + port);

        channelFuture
                .channel()
                .closeFuture()
                .sync();//同步等待关闭
    }
}

