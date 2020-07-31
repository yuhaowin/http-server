package com.yuhaowin.core.servlet;

import com.yuhaowin.core.handler.HandlerManager;
import com.yuhaowin.core.handler.MappingHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.lang.reflect.InvocationTargetException;

public class DispatcherServlet  {

    public void service(HttpObject  req, ChannelHandlerContext ctx){
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

        String msg = "<html><head><title>http server demo base on netty</title></head><body>你请求 url 不存在："  +"</body></html>";
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
