package com.luopo.easySpringTest.component.goodAfternoon.goodAfternoonImpl;

import com.luopo.easySpring.spring.annotation.Component;
import com.luopo.easySpringTest.component.goodAfternoon.GoodAfternoon;

@Component
public class GoodAfternoonImpl implements GoodAfternoon {
    @Override
    public void good() {
        System.out.println("Good Afternoon!");

    }
}
