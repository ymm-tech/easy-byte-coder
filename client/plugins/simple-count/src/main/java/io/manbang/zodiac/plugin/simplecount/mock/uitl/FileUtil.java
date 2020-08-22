package io.manbang.easyByteCoder.plugin.simplecount.mock.uitl;


import java.io.*;

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




}
