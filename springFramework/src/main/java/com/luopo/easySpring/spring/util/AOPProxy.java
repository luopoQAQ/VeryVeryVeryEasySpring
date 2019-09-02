package com.luopo.easySpring.spring.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AOPProxy {

    public Object createProxy(Object aspect, //切面bean
                              Method beforeMethod, //前置通知注解的方法
                              Method afterMethod, //后置通知注解的方法
                              Object target, //切点所在类
                              String targetMethod  ) { //切点方法
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new InvocationHandler() {   //事件调用处理器
                    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                        //如果被代理类调用的方法，不是切面方法，则不进行通知，而是直接调用被代理方法即可
                        if (!method.getName().equals(targetMethod)) {
                            return method.invoke(target, args);
                        }

                        if (beforeMethod != null) {
                            // 调用aspect这个bean的beforeMethod方法
                            // （beforeMethod是一个被before注释的执行前置通知的具体方法）
                            beforeMethod.invoke(aspect);
                        }

                        Object ret = method.invoke(target, args);

                        if (afterMethod != null) {
                            afterMethod.invoke(aspect);
                        }

                        return ret;
                    }

                }
        );
    }


}
