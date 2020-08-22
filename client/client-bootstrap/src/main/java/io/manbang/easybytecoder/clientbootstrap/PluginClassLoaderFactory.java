package io.manbang.easybytecoder.clientbootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author GaoYang 2018/12/10
 */
public class PluginClassLoaderFactory {
    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoaderFactory.class);

    public static URLClassLoader createPluginClassLoader(final ClassLoader parentClassLoader, final File base, List<String> pluginJarNames) {

        logger.info("plugins to be loaded: {}", pluginJarNames);
        if (base == null || !base.canRead() || !base.isDirectory()) {
            return null;
        }

        File[] files = base.listFiles(new PluginJarFileFilter(pluginJarNames));

        if (null == files || 0 == files.length) {
            logger.error("not find any plugin jars in the directory:{}", base.getAbsolutePath());
            return null;
        }

        logger.info("find plugin jars in the directory: {} ,plugin count: {}", base.getAbsolutePath(), files.length);

        URL[] elements = new URL[files.length];

        for (int j = 0; j < files.length; j++) {
            try {
                URL element = files[j].toURI().normalize().toURL();
                elements[j] = element;
                logger.info("Adding '{}' to classloader", element.toString());

            } catch (MalformedURLException e) {
                logger.error("load jar file error", e);
            }
        }
        return URLClassLoader.newInstance(elements, parentClassLoader);
    }

    public static void closeIfPossible(URLClassLoader urlClassLoader) {

        // 如果是JDK7+的版本, URLClassLoader实现了Closeable接口，直接调用即可
        if (urlClassLoader instanceof Closeable) {
            try {
                ((Closeable) urlClassLoader).close();
            } catch (Throwable cause) {
                // ignore...
            }
            return;
        }


        // 对于JDK6的版本，URLClassLoader要关闭起来就显得有点麻烦，这里弄了一大段代码来稍微处理下
        // 而且还不能保证一定释放干净了，至少释放JAR文件句柄是没有什么问题了
        try {
            final Object sun_misc_URLClassPath = forceGetDeclaredFieldValue(URLClassLoader.class, "ucp", urlClassLoader);
            final Object java_util_Collection = forceGetDeclaredFieldValue(sun_misc_URLClassPath.getClass(), "loaders", sun_misc_URLClassPath);

            for (final Object sun_misc_URLClassPath_JarLoader :
                    ((Collection) java_util_Collection).toArray()) {
                try {
                    final JarFile java_util_jar_JarFile = forceGetDeclaredFieldValue(sun_misc_URLClassPath_JarLoader.getClass(), "jar", sun_misc_URLClassPath_JarLoader);
                    java_util_jar_JarFile.close();
                } catch (Throwable t) {
                    // if we got this far, this is probably not a JAR loader so skip it
                }
            }

        } catch (Throwable cause) {
            // ignore...
        }

    }

    private static  <T> T forceGetDeclaredFieldValue(Class<?> clazz, String name, Object target) throws NoSuchFieldException, IllegalAccessException {
        final Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static class PluginJarFileFilter implements FileFilter {

        private List<String> jarFileNames;

        public PluginJarFileFilter(List<String> jarFileNames) {
            this.jarFileNames = jarFileNames;
        }

        @Override
        public boolean accept(File f) {
            if (jarFileNames.contains(f.getName())) {
                return true;
            }
            return false;
        }
  }
}
