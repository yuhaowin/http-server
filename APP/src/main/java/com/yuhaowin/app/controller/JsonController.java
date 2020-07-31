package com.yuhaowin.app.controller;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

//@Servlet("/netty/json")
@Deprecated
public class JsonController  {

    //@Override
    protected void doPost(HttpRequest request, ChannelHandlerContext ctx) {
        HttpHeaders headers;
        headers = request.headers();
        String contentType = headers.get("Content-Type").trim();
        Map<String, Object> mapReturnData = new HashMap<String, Object>();

        byte[] reqContent = null;
        // 处理POST请求
        if (contentType.contains("application/json")) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) request;
            // 解析json数据
            ByteBuf content = fullHttpRequest.content();
             reqContent = new byte[content.readableBytes()];
            content.readBytes(reqContent);
            String strContent = null;
            try {
                strContent = new String(reqContent, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println("接收到的消息" + strContent);
        } else {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            ctx.writeAndFlush(response).addListener( ChannelFutureListener.CLOSE);
        }
        System.out.println("POST方式：" + mapReturnData.toString());

        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(reqContent));

        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
