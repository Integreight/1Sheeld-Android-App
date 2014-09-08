package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.database.Cursor;
import android.provider.MediaStore;

public class SocialUtils {
	public static final byte FROM_ONESHEELD_FOLDER = (byte) 0x00;
	public static final byte FROM_CAMERA_FOLDER = (byte) 0x01;
	
	public static String getLastCapturedImagePathFromCameraFolder(
			Activity activity) {
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
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
			System.out.println("!@!@   " + fullPath);
			if (fullPath.contains("DCIM")) {
				// --last image from camera --
				System.out.println(fullPath);
				break;
			}
		} while (imageCursor.moveToNext());
		return fullPath;
	}

	public static String getLastCapturedImagePathFromOneSheeldFolder(
			Activity activity) {
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
		Cursor imageCursor = activity.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				MediaStore.Images.Media.DATA + " like ? ",
				new String[] { "%OneSheeld%" }, imageOrderBy);
		imageCursor.moveToFirst();
		String fullPath = null;
		if (imageCursor.getCount() == 0)
			return null;
		do {
			fullPath = imageCursor.getString(imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			System.out.println("!@!@   " + fullPath);
			if (fullPath.contains("OneSheeld")) {
				// --last image from camera --
				System.out.println(fullPath);
				break;
			}
		} while (imageCursor.moveToNext());
		return fullPath;
	}
}
