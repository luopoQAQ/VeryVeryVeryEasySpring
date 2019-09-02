package com.luopo.easySpringTest.aspect;

import com.luopo.easySpring.spring.annotation.*;

@Aspect
@Component
public class GoodAspect {

    @Pointcut("com.luopo.easySpringTest.component.goodAfternoon.goodAfternoonImpl.GoodAfternoonImpl.good()")
    public void goodPoint() {
    }

    @Before("goodPoint()")
    public void goodMorning() {
        System.out.println("Good Morning!");

    }

    @After("goodPoint()")
    public void goodEvening() {
        System.out.println("Good Evening!");

    }


}
