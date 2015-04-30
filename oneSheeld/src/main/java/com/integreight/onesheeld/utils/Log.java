package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.OneSheeldApplication;

public class Log {
    public static void d(String tag, String msg) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0)
            android.util.Log.d(tag, msg);
    }

    public static void test(String tag, String msg) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0)
            android.util.Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0)
            android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0) {
            tr.printStackTrace();
            android.util.Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0)
            android.util.Log.e(tag, msg);
    }

    public static void sysOut(String msg) {
        if (OneSheeldApplication.isDebuggable() && msg != null && msg.trim().length() > 0)
            System.out.println(msg);
    }
}
