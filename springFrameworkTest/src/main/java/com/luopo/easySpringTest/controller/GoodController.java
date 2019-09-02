package com.luopo.easySpringTest.controller;

import com.luopo.easySpring.spring.annotation.AutoWired;
import com.luopo.easySpring.springMVC.annotation.Controller;
import com.luopo.easySpring.springMVC.annotation.RequestMapping;
import com.luopo.easySpringTest.component.goodAfternoon.GoodAfternoon;

@Controller
public class GoodController {

    @AutoWired
    GoodAfternoon goodAfternoon;

    @RequestMapping("/good")
    public String good() {
        goodAfternoon.good();
        return "Good Night!";
    }


}
