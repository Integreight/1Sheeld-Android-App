package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.OneSheeldApplication;

public class Log {
	public static void d(String tag, String msg) {
		if (OneSheeldApplication.isDebuggable())
			android.util.Log.d(tag, msg);
	}

	public static void test(String tag, String msg) {
		if (OneSheeldApplication.isDebuggable())
			android.util.Log.d(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (OneSheeldApplication.isDebuggable())
			android.util.Log.d(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (OneSheeldApplication.isDebuggable()) {
			tr.printStackTrace();
			android.util.Log.d(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (OneSheeldApplication.isDebuggable())
			android.util.Log.e(tag, msg);
	}

	public static void sysOut(String msg) {
		if (OneSheeldApplication.isDebuggable())
			System.out.println(msg);
	}
}
