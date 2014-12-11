package com.integreight.onesheeld.shields.controller.utils;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public class CameraUtils {
	public static Camera.Size getBiggesttPictureSize(
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

	/** Check if this device has a camera */
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

	/** Check if this device has front camera */
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
	
	/** Check if this device has flash */
	public static boolean hasFlash(Camera mCamera) {
//		Camera mCamera=Camera.open();
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
}
