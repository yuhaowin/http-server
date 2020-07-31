package com.yuhaowin.app;

import com.yuhaowin.core.start.NettyApplication;

/**
 * 应用启动类
 * com.yuhaowin.app.Application
 */
public class Application {
    public static void main(String[] args) {
        NettyApplication.run(Application.class, args);
    }
}
