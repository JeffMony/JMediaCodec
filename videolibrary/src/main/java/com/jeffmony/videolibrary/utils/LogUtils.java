package com.jeffmony.videolibrary.utils;

import android.util.Log;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class LogUtils {

    private static final String PRE_LOG = "JCODEC_";

    private static final boolean IS_VERBOS = false;
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_INFO = true;
    private static final boolean IS_WARN = true;
    private static final boolean IS_ERROR = true;

    public static void v(String tag, String msg) {
        if (IS_VERBOS) {
            Log.v(PRE_LOG + tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(PRE_LOG + tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (IS_INFO) {
            Log.i(PRE_LOG + tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (IS_WARN) {
            Log.w(PRE_LOG + tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IS_ERROR) {
            Log.e(PRE_LOG + tag, msg);
        }
    }
}
