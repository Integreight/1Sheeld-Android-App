package com.integreight.onesheeld;

public class Log {
	public static void d(String tag, String msg) {
		android.util.Log.d(tag, msg);
	}
	public static void e(String tag, String msg) {
		android.util.Log.e(tag, msg);
	}
}
