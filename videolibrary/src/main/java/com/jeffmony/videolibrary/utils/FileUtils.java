package com.jeffmony.videolibrary.utils;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class FileUtils {

    public static long getSize(@NonNull File file) {
        if (file.isDirectory()) {
            long size = 0;
            for (File f : file.listFiles()) {
                size += getSize(f);
            }
            return size;
        } else {
            return file.length();
        }
    }
}
