package com.yuhaowin.core.handler;

import com.yuhaowin.core.annotation.Autowired;
import com.yuhaowin.core.annotation.Bean;
import com.yuhaowin.core.annotation.Controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {

    private static Map<Class<?>, Object> classToBean = new ConcurrentHashMap<>();

    public static Object getBean(Class<?> clazz) {
        return classToBean.get(clazz);
    }

    public static void initBean(List<Class<?>> classList) throws InstantiationException, IllegalAccessException {
        List<Class<?>> list = new ArrayList<>(classList);
        while (list.size() != 0) {
            int classSize = list.size();
            for (int i = 0; i < list.size(); i++) {
                if (finishCreate(list.get(i))) {
                    list.remove(i);
                }
            }

            if (list.size() == classSize) {
                throw new RuntimeException("init Bean exception ");
            }

        }
    }

    private static boolean finishCreate(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if (!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(Bean.class)) {
            return true;
        }
        Object bean = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> fieldType = field.getType();
                Object reliantBean = getBean(fieldType);
                if (Objects.isNull(reliantBean)) {
                    return false;
                }
                field.setAccessible(true);
                field.set(bean, reliantBean);
            }
        }
        classToBean.put(clazz, bean);
        return true;
    }
}
