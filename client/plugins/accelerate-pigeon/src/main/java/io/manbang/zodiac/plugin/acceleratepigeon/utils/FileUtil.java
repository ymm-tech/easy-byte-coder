package io.manbang.easyByteCoder.plugin.acceleratepigeon.utils;


import io.manbang.easyByteCoder.runtimecommonapi.utils.EasyByteCoderResourceObjectPool;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * @author xujie
 */
public class FileUtil {

    public static void fWriter(String path, String name, String text) {
        //声明一个文件（创建文件）
        File file = null;
        //声明文件输出字节流
        FileOutputStream fos = null;
        //声明对象处理流
        OutputStreamWriter osw = null;
        try {
            name = name.replace("http:", "");
            name = name.replace("/", "^");

            name = name + ".json";
            file = new File(path + name);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            //向文件中写入对象的数据
            osw.write(text);
            //清空缓冲区
            osw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭资源
                fos.close();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Properties getDoomConfigProperties() {
        Properties doomConfigProperties = FileUtil.readProperties("./", "doomConfig");
        return doomConfigProperties;
    }


    public static String customDirectory() {
        Properties properties = getDoomConfigProperties();
        String directory = properties.getProperty("directory");
        if ("".equals(directory) || directory == null) {
            return "./";
        }
        return directory;
    }

    public static void updateProperties(String key, String value) {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("doomConfig.properties");
            config.setAutoSave(true);
            config.setProperty(key,value);
        } catch (ConfigurationException cex) {
            System.err.println("loading of the configuration file failed");
        }
    }

    public static Properties readProperties(String path, String name) {

        //声明一个文件（读取）
        File file = null;
        //声明文件输入字节流
        FileInputStream fos = null;
        //声明对象处理流
        InputStreamReader osw = null;
        name = name.replace("http:", "");
        name = name.replace("/", "^");

        name = name + ".properties";
        file = new File(path + name);
        if (!fileIsExists(path, name)) {
            return null;
        }

        try {
            fos = new FileInputStream(file);
            osw = new InputStreamReader(fos, "UTF-8");
            Properties properties = new Properties();
            // 使用InPutStream流读取properties文件
            BufferedReader br = new BufferedReader(osw);
            properties.load(br);
            return properties;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭资源
                fos.close();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String readFile(String path, String name) {
        //声明一个文件（读取）
        File file = null;
        //声明文件输入字节流
        FileInputStream fos = null;
        //声明对象处理流
        InputStreamReader osw = null;
        name = name.replace("http:", "");
        name = name.replace("/", "^");

        name = name + ".json";
        file = new File(path + name);
        if (!fileIsExists(path, name)) {
            return null;
        }

        try {

            fos = new FileInputStream(file);
            osw = new InputStreamReader(fos, "UTF-8");

            BufferedReader br = new BufferedReader(osw);

            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭资源
                fos.close();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return StringUtils.EMPTY;
    }

    public static boolean fileIsExists(String path, String name) {
        try {
            File file = null;
            name = name.replace("http:", "");
            name = name.replace("/", "^");

            file = new File(path + name);
            if (!file.getParentFile().exists()) {
                return false;
            }
            return file.exists();
        } catch (Exception e) {
            EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error("fileIsExists err path:{}  name:{}", path, name, e);
            return false;
        }

    }
}
