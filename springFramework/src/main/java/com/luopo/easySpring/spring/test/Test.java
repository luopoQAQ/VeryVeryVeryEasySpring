package com.luopo.easySpring.spring.test;

import com.luopo.easySpring.spring.util.AOPProxy;
import com.luopo.easySpring.spring.util.ClassScanner;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<Class<?>> classList = ClassScanner.scannerClass("com.luopo.easySpring");

        for (Class it : classList) {
            System.out.println(it);

        }

    }

}
