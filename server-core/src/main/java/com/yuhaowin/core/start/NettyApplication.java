package com.yuhaowin.core.start;

import com.yuhaowin.core.handler.BeanFactory;
import com.yuhaowin.core.handler.ClassScanner;
import com.yuhaowin.core.handler.HandlerManager;
import com.yuhaowin.core.server.HttpServer;

import java.util.Arrays;
import java.util.List;

public class NettyApplication {

    public static void run(Class<?> clazz, String[] args) {
        System.out.println("Hello NettyApplication");
        int port = 8080; //默认启动端口
        String commandPort = Arrays.stream(args)
                .filter(arg -> arg.startsWith("--server.port=")).findFirst().orElse("");
        if (commandPort != null && !commandPort.isEmpty()) {
            port = Integer.valueOf(commandPort.split("=")[1]);
        }
        HttpServer httpServer = new HttpServer(port);
        try {
            // 扫描指定包路径下的所有 class
            List<Class<?>> classList = ClassScanner.scanClass(clazz.getPackage().getName());
            BeanFactory.initBean(classList);
            // 从 controller 中解析所有的请求处理
            HandlerManager.resolveMappingHandler(classList);
            httpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer httpServer = new HttpServer(8080);
        httpServer.start();
    }
}
