package com.luopo.easySpring.springMVC.util;

import com.luopo.easySpring.springMVC.annotation.Controller;
import com.luopo.easySpring.springMVC.annotation.RequestMapping;
import com.luopo.easySpring.springMVC.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

//映射管理器的作用主要是对被@RequestMapping映射的方法进行封装，方便调用与访问
public class HandlerManager {
    //解析好的可以映射的方法都被封装成映射处理器放在这儿
    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();

    public static void resolveRequestMapping(List<Class<?>> classList) {
        System.out.println();
        for (Class<?> it : classList) {

            if (it.isAnnotationPresent(Controller.class)) {
                for (Method itMethod : it.getDeclaredMethods()) {
                    MappingHandler mappingHandler = new MappingHandler();

                    if (itMethod.isAnnotationPresent(RequestMapping.class)) {

                        //设置封装的映射处理器的uri
                        mappingHandler.setUri(
                                itMethod.getDeclaredAnnotation(RequestMapping.class).value());

                        //设置映射的处理器
                        mappingHandler.setController(it);

                        //设置映射的方法
                        mappingHandler.setMethod(itMethod);

                        //设置映射方法的参数
                        List<String> paramList = new ArrayList<>();
//                        System.out.println();
                        for (Parameter param : itMethod.getParameters()) {
                            if (param.isAnnotationPresent(RequestParam.class)) {
                                paramList.add(
                                        param.getAnnotation(RequestParam.class).value()
                                );
//                                System.out.println("add param : " + param.getAnnotation(RequestParam.class).value());
                            }
                        }
//                        System.out.println(paramList.size());

                        mappingHandler.setParams(paramList.toArray(new String[paramList.size()]));
                    }

                    System.out.println("解析映射 " + itMethod.getDeclaredAnnotation(RequestMapping.class).value()
                            + " 成功，封装为" + mappingHandler
                    );

                    mappingHandlerList.add(mappingHandler);
                }
            }

        }


    }


}
