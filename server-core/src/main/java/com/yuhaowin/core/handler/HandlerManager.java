package com.yuhaowin.core.handler;

import com.yuhaowin.core.annotation.Controller;
import com.yuhaowin.core.annotation.RequestMapping;
import com.yuhaowin.core.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 controller 中 解析 MappingHandler 放入 list 中
 */
public class HandlerManager {

    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();

    public static void resolveMappingHandler(List<Class<?>> classList) {
        for (Class<?> clazz : classList) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                parseHandlerFromController(clazz);
            }
        }
    }

    private static void parseHandlerFromController(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(RequestMapping.class)) {
                continue;
            }
            ArrayList<String> parameterList = new ArrayList<>();
            String uri = method.getDeclaredAnnotation(RequestMapping.class).value();
            String methodType = method.getDeclaredAnnotation(RequestMapping.class).methodType();
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                if (parameter.isAnnotationPresent(RequestParam.class)) {
                    parameterList.add(parameter.getDeclaredAnnotation(RequestParam.class).value());
                }
            }
            String[] args = parameterList.toArray(new String[parameterList.size()]);
            MappingHandler handler = new MappingHandler(uri, method, clazz, args, methodType);
            mappingHandlerList.add(handler);
        }
    }
}
