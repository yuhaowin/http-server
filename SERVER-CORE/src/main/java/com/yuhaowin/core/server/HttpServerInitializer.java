package com.yuhaowin.core.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        // http 编解码 获取uri中的参数
        pipeline.addLast(new HttpServerCodec());

        /**
         * http服务器端对request解码
         */
        //pipeline.addLast(new HttpRequestDecoder());

        /**
         * http服务器端对response编码
         */
        //pipeline.addLast(new HttpResponseEncoder());

        /**
         * http 消息聚合器 512*1024 为接收的最大 contentlength
         * POST方式请求服务器的时候，对应的参数信息是保存在http 的 body中的,
         * 如果只是用HttpServerCodec是无法完全的解析Http POST请求的，
         * 因为HttpServerCodec只能获取uri中参数，所以需要加上HttpObjectAggregator.
         *
         * 添加后将不会接收到 httpContent 类型的数据 必须在 HttpRequestDecoder 和 HttpResponseEncoder 之后
         */
        pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024));

        /**
         * 在HttpResponseEncoder序列化之前会对response对象进行HttpContentCompressor压缩
         */
        //pipeline.addLast(new HttpContentCompressor());

        // 请求处理器
        pipeline.addLast(new HttpRequestHandler());
    }
}
