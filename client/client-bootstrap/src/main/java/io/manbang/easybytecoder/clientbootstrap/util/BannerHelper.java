package io.manbang.easybytecoder.clientbootstrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BannerHelper {
    public static void banner(String path) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(path)));
            String str = "";
            while ((str = in.readLine()) != null) {
                System.out.println(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

