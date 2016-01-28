package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.Log;

import java.io.File;
import java.util.List;

public class CameraUtils {
    public static final byte FROM_ONESHEELD_FOLDER = (byte) 0x00;
    public static final byte FROM_CAMERA_FOLDER = (byte) 0x01;
    public static String CAMERA_CAPTURE_RECEIVER_EVENT_NAME = "camera_capture_event_name";
    public static String lastCapturedImagePathFromOneSheeldFolder = "";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static final String sharedPreferencesKey = "LastImage";

    public static Camera.Size getBiggestPictureSize(
            Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    /**
     * Check if this device has a camera
     */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Check if this device has front camera
     */
    public static boolean checkFrontCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has front camera
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }

    /**
     * Check if this device has flash
     */
    public static boolean hasFlash(Camera mCamera) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

    public static String getLastCapturedImagePathFromCameraFolder(
            Activity activity) {
        final String[] imageColumns = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = activity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                null, null, imageOrderBy);
        imageCursor.moveToFirst();
        String fullPath = null;
        if (imageCursor.getCount() == 0)
            return null;
        do {
            fullPath = imageCursor.getString(imageCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            Log.sysOut("!@!@   " + fullPath);
            if (fullPath.contains("DCIM")) {
                // --last image from camera --
                Log.sysOut(fullPath);
                break;
            }
        } while (imageCursor.moveToNext());
        return fullPath;
    }

    public static String getLastCapturedImagePathFromOneSheeldFolderInMediaStore(
            Activity activity) {
        final String[] imageColumns = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = activity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[]{"%OneSheeld%"}, imageOrderBy);
        imageCursor.moveToFirst();
        String fullPath = null;
        if (imageCursor.getCount() == 0)
            return null;
        do {
            fullPath = imageCursor.getString(imageCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            Log.sysOut("!@!@   " + fullPath);
            if (fullPath.contains("OneSheeld")) {
                // --last image from camera --
                Log.sysOut(fullPath);
                break;
            }
        } while (imageCursor.moveToNext());
        return fullPath;
    }

    public static String getLastCapturedImagePathFromOneSheeldFolder(Activity activity,boolean viewToast) {
        sharedPreferences = activity.getSharedPreferences("camera", Context.MODE_PRIVATE);
        File tmpImage;
        if (lastCapturedImagePathFromOneSheeldFolder != null) {
            tmpImage = new File(lastCapturedImagePathFromOneSheeldFolder);
            if (tmpImage.exists())
                return lastCapturedImagePathFromOneSheeldFolder;
            else {
                lastCapturedImagePathFromOneSheeldFolder = sharedPreferences.getString(sharedPreferencesKey,"");
                tmpImage = new File(lastCapturedImagePathFromOneSheeldFolder);
                if (tmpImage.exists())
                    return lastCapturedImagePathFromOneSheeldFolder;
                else
                    return getLastCapturedImagePathFromOneSheeldFolderInMediaStore(activity);
            }
        }else{
            lastCapturedImagePathFromOneSheeldFolder = sharedPreferences.getString(sharedPreferencesKey,"");
            tmpImage = new File(lastCapturedImagePathFromOneSheeldFolder);
            if (tmpImage.exists())
                return lastCapturedImagePathFromOneSheeldFolder;
        }
        if (viewToast)
            Toast.makeText(activity.getApplicationContext(), R.string.camera_image_not_found_toast,Toast.LENGTH_SHORT).show();
        return "";
    }

    public static void setLastCapturedImagePathFromOneSheeldFolder(String lastCapturedImagePathFromOneSheeldFolder,Activity activity) {
        sharedPreferences = activity.getSharedPreferences("camera",Context.MODE_PRIVATE);
        if (lastCapturedImagePathFromOneSheeldFolder != null) {
            File tmpImage = new File(lastCapturedImagePathFromOneSheeldFolder);
            if (tmpImage.exists()) {
                CameraUtils.lastCapturedImagePathFromOneSheeldFolder = lastCapturedImagePathFromOneSheeldFolder;
                editor = sharedPreferences.edit();
                editor.putString(sharedPreferencesKey,lastCapturedImagePathFromOneSheeldFolder);
                editor.commit();
            }
        }
    }
}