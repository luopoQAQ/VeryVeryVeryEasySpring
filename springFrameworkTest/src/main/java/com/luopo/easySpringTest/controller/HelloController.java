package com.luopo.easySpringTest.controller;

import com.luopo.easySpring.spring.annotation.AutoWired;
import com.luopo.easySpring.springMVC.annotation.Controller;
import com.luopo.easySpring.springMVC.annotation.RequestMapping;
import com.luopo.easySpring.springMVC.annotation.RequestParam;
import com.luopo.easySpringTest.component.HelloService;

@Controller
public class HelloController {

    @AutoWired
    HelloService helloService;

    @RequestMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        if (name == null) {
            return "未指定name";
        }

        return helloService.hello(name);
    }

}
