package com.luopo.easySpring.spring.util;
import com.luopo.easySpring.spring.annotation.*;
import com.luopo.easySpring.springMVC.annotation.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {

    //IOC容器，存放类对应的bean（对象）
    public static Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    //已经自动装配好的类
    public static Set<Class<?>> hasAutoWiredBeans =
            Collections.synchronizedSet(new HashSet<>());

    public static void initBean(List<Class<?>> classList) throws Exception {
        List<Class<?>> needCreateClassList = new ArrayList<>();
        List<Class<?>> aspectClassList = new ArrayList<>();

        //将需要处理的类取出
        for (Class it : classList) {
            //先将Aspect加入list，方便后面直接对其进行处理
            if (it.isAnnotationPresent(Aspect.class)) {
                aspectClassList.add(it);
//                System.out.println(it);
            }

            if (it.isAnnotationPresent(Component.class)
                    || it.isAnnotationPresent(Controller.class)) {
                needCreateClassList.add(it);
            }
        }

        while (!needCreateClassList.isEmpty()) {
            List<Class<?>> removeList = new ArrayList<>();

            for (Class it : needCreateClassList) {
                //对所有需要注入的bean进行注册，按道理说只要一个pass类至少注册成功一个
                //遍历该类，判断是否需要注入依赖（是否被注解标识为bean）
                //如果不需要注入依赖，则直接从list中删除
                //如果需要注入依赖，则遍历field，将被AutoWired标识的field进行填充
                //只要有一个填充失败，则说明该AutoWired没有bean（忘记注册），则报错
                if (createBean(it, true)) {
                    //注入成功则将其加入删除list中，等待迭代结束删除
                    //（为什么不能直接删除？因为会有并发改异常（不能一边迭代一边remove））
                    removeList.add(it);
                    System.out.println("bean ：" + it + "注册成功");
                }

            }   //一趟注入bean结束

            if (removeList.isEmpty()) {     //若为空，则说明剩下的bean都不可注册了，抛异常
                String msg = "需要注入的bean不存在或是陷入循环依赖\n";
                msg += "\n未初始化的类集合：" + needCreateClassList;
                msg += "\n已初始化类集合：" + beans;
                throw new Exception(msg);
            }
            else {      //否则的话就取出已经注册好的bean的class，从待注册list删除
                for (Class<?> removeClass : removeList) {
                    needCreateClassList.remove(removeClass);
                }
            }
        }

        //初步装入bean成功，则使用AOP重新装入bean
        if (aspectClassList.isEmpty()) {
            return ;
        }

        for (Class<?> aspectClass : aspectClassList) {      //处理所有的切面类
            Method beforeMethod = null;
            Method afterMethod = null;
            Object proxyTarget = null;
            String proxyMethod = null;
            String pointcutName = null;

            Object aspectBean = aspectClass.newInstance();
            for (Method m : aspectBean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Pointcut.class)) {    //获取切点类及切点方法
                    String pointcut = m.getAnnotation(Pointcut.class).value();
                    String classStr = pointcut.substring(0, pointcut.lastIndexOf("."));
                    proxyTarget = Thread.currentThread().getContextClassLoader()
                            .loadClass(classStr).newInstance();
                    proxyMethod = pointcut.substring(pointcut.lastIndexOf(".") + 1);
                    pointcutName = m.getName();     //获取切点签名（不带括号）

                    if (proxyTarget == null || proxyMethod == null) {
                        throw new Exception("切点定义错误");
                    }

                }
            }

            //获取所有的通知方法及通过动态代理处理
            for (Method m : aspectBean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Before.class)) {
                    String value = m.getAnnotation(Before.class).value();
                    String beforePointcutName = value.substring(0, value.indexOf("("));

                    if (pointcutName == null) {
                        throw new Exception("未定义切点签名");
                    }

                    if (pointcutName.equals(beforePointcutName)) {
                        beforeMethod = m;
                    }
                }

                if (m.isAnnotationPresent(After.class)) {
                    String value = m.getAnnotation(After.class).value();
                    String afterPointcutName = value.substring(0, value.indexOf("("));

                    if (pointcutName == null) {
                        throw new Exception("未定义切点签名");
                    }

                    if (pointcutName.equals(afterPointcutName)) {
                        afterMethod = m;
                    }
                }
            }

            System.out.println("\nASPECT处理：");
            System.out.println(proxyTarget);
            System.out.println(proxyMethod);
            System.out.println();

            //利用动态代理实现AOP，先获取代理对象，再重新注入
            Object proxy = new AOPProxy().createProxy(aspectBean, beforeMethod, afterMethod,
                    proxyTarget, proxyMethod.substring(0, proxyMethod.indexOf("(")));

            if (BeanFactory.beans.containsKey(proxyTarget.getClass())) {    //更新原bean为代理类
                BeanFactory.beans.put(proxyTarget.getClass(), proxy);
            } else {
                throw new Exception("被代理类不存在或未被注入bean" + proxyTarget.getClass());
            }
        }

        //将保存的需要注入的bean重新注入一遍，因为切面已经更新代理了一些bean
        for (Class<?> it : hasAutoWiredBeans) {
            if (!createBean(it, false)) {
                throw new Exception("AOP重新注入依赖失败" + it);
            }
            System.out.println("AOP重新注入 " + it + "成功");
        }
    }

    private static boolean createBean(Class<?> it, boolean addHasAutoWiredBeans) throws Exception {
        Object itBean = it.newInstance();
        for (Field field : itBean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoWired.class)) {
                //使用set保存被注入属性的bean，因为有重复，所以用set(而不需要注入依赖的bean则不需要在此处理)
                if (addHasAutoWiredBeans) {
                    BeanFactory.hasAutoWiredBeans.add(it);
                }

                Class<?> fieldType = field.getType();

                Object needWiredBean = BeanFactory.beans.get(fieldType);    //需要注册的bean

                System.out.println("    >> 直接从bean中获得: " + needWiredBean);

                List<Object> needWiredBeanList = new ArrayList<>();     //如果是接口，子类实现的bean（可能为list）
                if (needWiredBean == null) {    //需要的bean不存在，则需要分情况讨论
                    if (!fieldType.isInterface()) {     //如果不是接口，那就说明需要注入的bean不存在
//                                throw new Exception("需要注入的bean不存在:" + fieldType.getName());
                        return false;  //不能抛出异常，可能在别的尚未注入的bean那里
                    }
                    else {      //如果是接口，则寻找其实现是否存在
                        for (Class<?> beanClass : BeanFactory.beans.keySet()) {
                            if (fieldType.isAssignableFrom(beanClass)) {
                                //获取子类bean，存入list
                                needWiredBeanList.add(BeanFactory.beans.get(beanClass));
                            }
                        }

                        if (needWiredBeanList.isEmpty()) {
//                            throw new Exception("需要注入的bean不存在:" + fieldType.getName())
                            return false;  //不能抛出异常，可能在别的尚未注入的bean那里
                        } else if (needWiredBeanList.size() == 1) {
                            field.setAccessible(true);
                            field.set(itBean, needWiredBeanList.get(0));    //注入唯一的bean，根据接口获得唯一子类bean

                            System.out.println("    >> 根据接口从bean中获得子类bean: " + needWiredBeanList.get(0));

                        }
                        else {  //如果发现该bean的子类不止一个，则说明bean重复，抛出异常
                            throw new Exception("可选择的bean重复：" + fieldType.getName());
                        }
                    }
                }
                else {
                    field.setAccessible(true);
                    field.set(itBean, needWiredBean);    //注入唯一的bean，从beans中直接获得
                }


            }
        }

        //注入成功则放入已经注册好的bean list中
        beans.put(it, itBean);
        return true;
    }


}
