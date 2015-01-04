package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class SensorUtil {
    public static PackageManager PM;
    public static boolean sensor;

    public static Boolean isDeviceHasSensor(String sensorType, Context mContext) {

        PM = mContext.getPackageManager();
        sensor = PM.hasSystemFeature(sensorType);
        if (sensorType.contains("Orientation") || sensorType.contains("Gravity") || sensorType.contains("Temperature")) {
            sensor = true;
        }
        return sensor;
    }

}
