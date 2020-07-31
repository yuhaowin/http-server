package com.yuhaowin.app.controller;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

//@Servlet("/netty/get-method")
@Deprecated
public class GetMethodController  {

   // @Override
    protected void doGet(HttpRequest request, ChannelHandlerContext ctx) {
        Map<String, String> parmMap = new HashMap<String, String>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        decoder.parameters().entrySet().forEach( entry -> {
            parmMap.put(entry.getKey(), entry.getValue().get(0));
        });

        String msg = "<html><head><title>http server demo base on netty</title></head><body>你请求uri为：" + request.uri() +"</body></html>";
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
