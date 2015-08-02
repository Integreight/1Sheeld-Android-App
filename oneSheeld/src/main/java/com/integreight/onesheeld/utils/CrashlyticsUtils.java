package com.integreight.onesheeld.utils;

public class CrashlyticsUtils {
    public static void setString(String key, String value) {
        try {
            com.crashlytics.android.Crashlytics.setString(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logException(Throwable throwable) {
        try {
            com.crashlytics.android.Crashlytics.logException(throwable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        try {
            com.crashlytics.android.Crashlytics.log(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
