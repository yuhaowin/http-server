package com.yuhaowin.core.dispatcher;

import com.yuhaowin.core.handler.HandlerManager;
import com.yuhaowin.core.handler.MappingHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.*;

import java.lang.reflect.InvocationTargetException;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class Dispatcher {

    private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};

    public void service(HttpObject req, ChannelHandlerContext ctx) {

        for (MappingHandler handler : HandlerManager.mappingHandlerList) {
            try {
                if (handler.handle(req, ctx)) {
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (req instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) req;
            System.out.println("test hello word");

            boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
            FullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(), OK, Unpooled.wrappedBuffer(CONTENT));
            response.headers()
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive) {
                System.out.println("this is keep alive");
                if (!httpRequest.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, CLOSE);
            }

            ChannelFuture f = ctx.write(response);

            ChannelId id = ctx.channel().id();
            System.out.println("socket channel id" + id);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
//
//        String msg = "<html><head><title>http server demo base on netty</title></head><body>你请求 url 不存在："  +"</body></html>";
//        // 创建http响应
//        FullHttpResponse response = new DefaultFullHttpResponse(
//                HttpVersion.HTTP_1_1,
//                OK,
//                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
//        // 设置头信息
//        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//        response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
//        // 将html write到客户端
//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
