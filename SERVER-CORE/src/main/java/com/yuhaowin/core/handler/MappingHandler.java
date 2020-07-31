package com.yuhaowin.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingHandler {

    private String uri;

    private Method method;

    private Class<?> controller;

    private String[] args;

    /**
     * http 请求类型
     */
    private String methodType;

    public MappingHandler(String uri, Method method, Class<?> clazz, String[] args,String methodType) {
        this.uri = uri;
        this.method = method;
        this.controller = clazz;
        this.args = args;
        this.methodType = methodType;
    }

    public boolean handle(HttpObject  req, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        HttpRequest request = (HttpRequest) req;
        // 获取请求的 uri
        String requestURI = request.uri();
        if (requestURI.contains("?")){
            requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
        if (!uri.equals(requestURI)) {
            return false;
        }
        System.out.println("请求的 url = " + requestURI);
        Object result = null;
        Object[] argsValues = new Object[args.length];

        if ("GET".equals(methodType)){
            System.out.println("GET 请求");
            for (int i = 0; i < args.length; i++) {
                argsValues[i] = getMethodParam(request).get(args[i]);
            }
        }

        if ("POST".equals(methodType)){
            HttpHeaders headers = request.headers();
            String contentType = headers.get("Content-Type").trim();
            System.out.println("POST 请求");
            if (contentType.contains("application/json")){
                argsValues[0] = postMethodParam(request).get("json-content");
            }else if (contentType.contains("x-www-form-urlencoded")){
                for (int i = 0; i < args.length; i++) {
                    argsValues[i] = postMethodParam(request).get(args[i]);
                }
            }else if (requestURI.contains("upload")) {
                argsValues[0] = readUploadFile(req);
            }
        }

        Object instance = BeanFactory.getBean(controller);
        result = method.invoke(instance, argsValues);

        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(result.toString().getBytes()));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        return true;
    }

    /**
     * 获取 from 的请求参数键值对
     * @param req
     * @return
     */
    private Map getMethodParam(HttpRequest req){
        Map<String, String> parmMap = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        decoder.parameters().entrySet().forEach( entry -> {
            parmMap.put(entry.getKey(), entry.getValue().get(0));
        });
        return parmMap;
    }

    /**
     * 获取去 body 中的参数
     * @param req
     * @return
     */
    private Map postMethodParam(HttpRequest req){
        HttpHeaders headers;
        headers = req.headers();
        String contentType = headers.get("Content-Type").trim();
        Map<String, Object> params = new HashMap<>();
        if (contentType.contains("x-www-form-urlencoded")) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
            List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
            for (InterfaceHttpData data : postData) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    MemoryAttribute attribute = (MemoryAttribute) data;
                    params.put(attribute.getName(),attribute.getValue());
                }
            }
        }else if (contentType.contains("application/json")) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) req;
            // 解析json数据
            ByteBuf content = fullHttpRequest.content();
            byte[] reqContent = new byte[content.readableBytes()];

            content.readBytes(reqContent);
            String strContent = null;
            try {
                strContent = new String(reqContent, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println("接收到的消息" + strContent);
            params.put("json-content",strContent);
        }
        return params;
    }


    protected File readUploadFile(HttpObject msg)  {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            if (request.method().equals(HttpMethod.OPTIONS)) { //处理跨域请求
                return null;
            } else if (request.method().equals(HttpMethod.POST)){ //文件通过post进行上传
                try {
                    //"multipart/form-data" ： 代表在表单中进行文件上传
                    if (!request.headers().get(HttpHeaderNames.CONTENT_TYPE).contains("multipart/form-data")){
                        return null;
                    }
                    /**
                     * 控制上传文件时内存/硬盘的比值，防止出现内存溢出
                     */
                    HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
                    /**
                     * 解码并处理 post 请求的 body
                     */
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
                    List<InterfaceHttpData> dataList = decoder.getBodyHttpDatas();
                    if (dataList == null) {
                        return null;
                    }
                    //for (int ni = 0; ni < dataList.size(); ni++) {
                    return writeHttpData(dataList.get(0));
                    //}
                    // decoder.destroy();
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    private File writeHttpData(InterfaceHttpData data) {
        File dest = null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
            FileUpload fileUpload = (FileUpload) data;
            if (fileUpload.isCompleted()) {
                File dir = new File(  "/download/");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                dest = new File(dir, fileUpload.getFilename());
                try {
                    fileUpload.renameTo(dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dest;
    }
}



