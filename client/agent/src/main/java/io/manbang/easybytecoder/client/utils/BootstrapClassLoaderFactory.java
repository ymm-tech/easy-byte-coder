package io.manbang.easybytecoder.client.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author GaoYang 2018/12/23
 */
public class BootstrapClassLoaderFactory {
    public static final String BOOTSTRAP_FILENAME = "bootstrap.jar";
    public static final String COMMON_FILENAME = "common.jar";

    public static URLClassLoader createBootClassLoader(String bootstrapClassPath, final ClassLoader oldLoader) {

        URL[] elements = new URL[2];
        File[] files = new File[2];
        files[0] = new File(bootstrapClassPath + "/" + BOOTSTRAP_FILENAME);
        files[1] = new File(bootstrapClassPath + "/" + COMMON_FILENAME);
        for (int j = 0; j < files.length; j++) {
            try {
                URL element = files[j].toURI().normalize().toURL();
                elements[j] = element;
            } catch (MalformedURLException e) {
                System.out.println("load jar file error:" + e);
            }
        }
        return URLClassLoader.newInstance(elements, oldLoader);
  }
}
