package com.luopo.easySpring.spring.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    public static List<Class<?>> scannerClass(String packageName)
            throws IOException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");

//        System.out.println("    >> path : " + path);

        Enumeration<URL> urlEnumeration = classLoader.getResources(path);

        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();

//            System.out.println("    >> url : " + url);
//            System.out.println("    >> url.getProtocol() : " + url.getProtocol());

            if (url.getProtocol().contains("jar")) {

                JarURLConnection jarURLConnection = (JarURLConnection)url.openConnection();
                String jarFilePath = jarURLConnection.getJarFile().getName();

//                System.out.println("    >> jarFilePath : " + jarFilePath);

                JarFile jarFile = new JarFile(jarFilePath);
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement();

                    String jarEntryName = jarEntry.getName();

//                    System.out.println("    >> jarEntryName : " + jarEntryName);

                    if (jarEntryName.startsWith(path) && jarEntryName.endsWith(".class")) {
                        String classFullName = jarEntryName.replace("/", ".")
                                .substring(0, jarEntryName.length() - 6);
                        classList.add(Class.forName(classFullName));

//                        System.out.println("    >> classFullName : " + classFullName);

                    }

                }
            }
        }

        return classList;
    }


}
