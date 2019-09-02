package com.luopo.easySpringTest.component;

import com.luopo.easySpring.spring.annotation.Component;

@Component
public class HelloService {
    public String hello(String name) {
        return new String("Hello " + name + "!");
    }
}
