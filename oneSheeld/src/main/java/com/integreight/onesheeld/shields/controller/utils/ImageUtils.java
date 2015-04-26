package com.integreight.onesheeld.shields.controller.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.integreight.onesheeld.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ImageUtils {
    public static Bitmap decodeFile(File f, int required_size) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = required_size;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private static Bitmap bitmap = null;

    public static Bitmap decodeBitmap(byte[] data, Matrix matrix) {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs
        // free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will
        // be
        // used to recover the Bitmap
        // data
        // after being clear, when it
        // will
        // be used in the future
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        if (matrix != null)
            matrix.reset();
        matrix = null;
        System.gc();

        return bitmap;
    }

    public static int getAverageColor(Bitmap bitmap) {
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
//        int pixelCount = 0;
        int intArray[] = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int y = 0; y < intArray.length; y++) {
//            for (int x = 0; x < bitmap.getWidth(); x++) {
            int c = intArray[y];

//                pixelCount++;
            redBucket += Color.red(c);
            greenBucket += Color.green(c);
            blueBucket += Color.blue(c);
            // does alpha matter?
//            }
        }

        int RGB = Color.rgb(redBucket / intArray.length, greenBucket / intArray.length,
                blueBucket / intArray.length);
        return RGB;
    }

    public static int getMostDominantColor(Bitmap bm/* , Activity activity */) {

        // Bitmap bm = ImageTester.getBitmapFromAsset(
        // activity.getApplicationContext(), "colors.bmp");

        int height = bm.getHeight();
        int width = bm.getWidth();

        Map<Integer, Integer> m = new HashMap<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = bm.getPixel(i, j);
                // int[] rgbArr = getRGBArr(rgb);
                // Filter out grays....
                // if (!isGray(rgbArr)) {
                Integer counter = m.get(rgb);
                if (counter == null)
                    counter = 0;
                counter++;
                m.put(rgb, counter);
                // }
            }
        }
        // String colourHex = getMostCommonColour(m);
        List<Map.Entry> list = new LinkedList<Map.Entry>(m.entrySet());

        Collections.sort(list, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map.Entry me = (Map.Entry) list.get(list.size() - 1);
        int[] rgb = getRGBArr((Integer) me.getKey());
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
        // return getMostCommonColour(m);
    }

    public static int[] getRGBArr(int color) {
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = (color) & 0xff;
        return new int[]{red, green, blue};

    }

//    public static int convert32Colorto2(int color) {
//        int a, r, g, b;
//        a = Color.alpha(color);
//        r = Color.red(color);
//        g = Color.green(color);
//        b = Color.blue(color);
//        if (r >> 7 == 1)
//            r = 0xff;
//        else
//            r = 0x00;
//        if (g >> 7 == 1)
//            g = 0xff;
//        else
//            g = 0x00;
//        if (b >> 7 == 1)
//            b = 0xff;
//        else
//            b = 0x00;
//        return (a << 24) | (r << 16) | (g << 8) | (b);
//    }

    public static String getHexColor(int color) {
        String hexColor = String.format("#%06X", 0xFFFFFF & color);
        return hexColor;

    }


}