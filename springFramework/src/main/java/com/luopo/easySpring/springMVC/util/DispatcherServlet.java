package com.luopo.easySpring.springMVC.util;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DispatcherServlet implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    //重写该servlet，并扔给tomcat
    //该servlet主要是将得到的请求扔给映射处理器，映射处理器全都被封装好存在银蛇处理器的管理器中
    //直接遍历该映射管理器，如果有匹配的映射处理器，就处理，并将结果写入响应
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        for (MappingHandler mappingHandler : HandlerManager.mappingHandlerList) {
            try {
                //有可以处理的映射处理器，则处理，将结果写入响应，并返回
                if (mappingHandler.handle(req, res)) {
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        //否则响应体写入错误信息
        res.getWriter().println("404 Not Found!");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
