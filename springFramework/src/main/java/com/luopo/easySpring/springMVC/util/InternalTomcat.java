package com.luopo.easySpring.springMVC.util;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

public class InternalTomcat {
    private Tomcat tomcat;
    private String[] args;

    public InternalTomcat(String[] args) {
        this.args = args;
    }

    public void start() throws LifecycleException {
        tomcat = new Tomcat();  //创建tomcat服务器
        tomcat.setPort(8080);   //设置端口号
        tomcat.start();         //启动

        Context context = new StandardContext();    //创建上下文
        context.setPath("");                        //设置访问根path
        context.addLifecycleListener(new Tomcat.FixContextListener());  //设置生命周期监听器

        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        //创建servlet（自己实现的），并设置异步
        Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet)
                .setAsyncSupported(true);

        context.addServletMappingDecoded(
                "/", "dispatcherServlet");     //servlet的url映射根路径
        tomcat.getHost().addChild(context);     //将上下文加入到tomcat

        //避免jvm因为tomcat全为守护线程而停止（直接new一个线程并阻塞）
        Thread tomcatAwaitThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InternalTomcat.this.tomcat.getServer().await();
                    }
                });
        tomcatAwaitThread.setDaemon(false);
        tomcatAwaitThread.start();
    }



}
