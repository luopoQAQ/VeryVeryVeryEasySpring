package com.luopo.easySpring;

import com.luopo.easySpring.spring.util.BeanFactory;
import com.luopo.easySpring.spring.util.ClassScanner;
import com.luopo.easySpring.springMVC.util.HandlerManager;
import com.luopo.easySpring.springMVC.util.InternalTomcat;

import java.util.List;

public class EasySpringEnter {
    public static void run(Class<?> clazz, String[] args) {
        InternalTomcat internalTomcat = new InternalTomcat(args);   //新建内置tomcat

        try {
            //启动tomcat
            internalTomcat.start();

            //扫描该主题包下所有类（jar）
            List<Class<?>> classList =
                    ClassScanner.scannerClass(clazz.getPackage().getName());

            //初始化bean
            BeanFactory.initBean(classList);

            //将@RequestMapping注解的方法依次封装为HandlerMapping
            HandlerManager.resolveRequestMapping(classList);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
