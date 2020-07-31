package com.yuhaowin.core.server;

import com.yuhaowin.core.servlet.DispatcherServlet;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject > {

    /**
     * 使用以下代码配置服务器对文件的缓存策略：
     */
    static {
        //DiskFileUpload -> 文件上传的磁盘实现
        //退出时删除临时文件
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 通过系统调用及时将 page cache 写入磁盘。
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject  req) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.service(req,ctx);
    }
}