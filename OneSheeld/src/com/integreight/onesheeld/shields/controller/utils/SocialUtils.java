package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.database.Cursor;
import android.provider.MediaStore;

public class SocialUtils {
	public static String getLastCapturedImagePath(Activity activity) {
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
		Cursor imageCursor = activity.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				null, null, imageOrderBy);
		imageCursor.moveToFirst();
		String fullPath = null;
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
}
