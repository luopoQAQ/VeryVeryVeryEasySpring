package com.luopo.easySpring.springMVC.util;

import com.luopo.easySpring.spring.util.BeanFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//这是一个封装类
//封装了一个Controller中的一个映射方法
//可以通过uri的匹配来获取该类
//该封装类的controller Bean 、 method 及 args可以调用相应的映射方法
public class MappingHandler {
    private String uri;
    private Class<?> controller;
    private Method method;
    private String[] params;

    public boolean handle(ServletRequest request, ServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        String requestURI = ((HttpServletRequest) request).getRequestURI();

        if (requestURI.equals(uri)) {
            Object[] objectParams = new Object[params.length];

            //从request中获取参数信息
            for (int i = 0; i < params.length; i++) {
                objectParams[i] = request.getParameter(params[i]);
//                System.out.println(objectParams[i]);
            }

            //获取控制器类的bean
            Object controller = BeanFactory.beans.get(this.controller);
            //利用反射调用控制器类对应的映射处理方法，并得到返回结果
            Object responseRet = method.invoke(controller, objectParams);

//            System.out.println(responseRet.toString());

            //写入response
            response.getWriter().println(responseRet.toString());

            return true;
        }

        //大部分情况都是未匹配（或是处理失败？）
        return false;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setController(Class<?> controller) {
        this.controller = controller;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
