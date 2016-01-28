package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.integreight.onesheeld.R;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class CameraHeadService extends Service implements
        SurfaceHolder.Callback {

    public static final int GET_RESULT = 1, CAPTURE_IMAGE = 3, CRASHED = 13, SET_CAMERA_PREVIEW_TYPE = 15;
    public final static int SHOW_PREVIEW = 7;
    public final static int HIDE_PREVIEW = 8;
    public final static int INVALIDATE_PREVIEW = 16;
    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;
    public static boolean isRunning = false;
    private static Handler queue = new Handler();
    private final int quality = 50;
    // the camera parametersrivate Bitmap bmp;
    FileOutputStream fo;
    SurfaceView sv;
    WindowManager.LayoutParams params;//, params1, params2, params3;
    SharedPreferences pref;
    Editor editor;
    int width = 0, height = 0;
    ByteArrayOutputStream out;
    Camera.Parameters parameters;
    Camera.Size size;
    YuvImage yuv;
    byte[] bytes;
    Bitmap bitmap;
    Bitmap resizebitmap;
    Handler handler = new Handler();
    int[] previewCells;
    int[] lastPreviewCells;
    int currentColorIndex = 0;
    int w = 0;
    int h = 0;
    int x = 0;
    int y = 0;
    // Camera variables
    // a surface holder
    // a variable to control the camera
    private Camera mCamera;
    private Bitmap bmp;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0;
    private boolean isFrontCamRequest = false;
    private Camera.Size pictureSize;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    private ArrayList<String> registeredShieldsIDs = new ArrayList<>();
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = -1;
    private boolean takenSuccessfully = false;
    private byte[] data;
    private Messenger colorDetectionMessenger;
    private Messenger cameraMessenger;
    private int cellSize = 1;
    private int currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
    private ColorDetectionShield.RECEIVED_FRAMES recevedFrameOperation = ColorDetectionShield.RECEIVED_FRAMES.CENTER;
    private ColorDetectionShield.COLOR_TYPE colorType = ColorDetectionShield.COLOR_TYPE.COMMON;
    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data1, final Camera camera) {
            if (colorDetectionMessenger != null) {
                queue.removeCallbacks(null);
                data = data1;
                queue.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data != null && camera != null) {
                            try {
                                Log.e("", "onPreviewFrame pass");
                                out = new ByteArrayOutputStream();
                                parameters = camera.getParameters();
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
//                                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                                } else if (parameters.getSupportedFocusModes().contains(
//                                        Camera.Parameters.FOCUS_MODE_AUTO)) {
//                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                                }
//                                camera.setParameters(parameters);
                                size = parameters.getPreviewSize();
                                yuv = new YuvImage(data, ImageFormat.NV21, size.width,
                                        size.height, null);

                                // bWidth and bHeight define the size of the bitmap you wish the
                                // fill with the preview image
                                yuv.compressToJpeg(new Rect(0, 0, size.width, size.height),
                                        quality, out);
                                bytes = out.toByteArray();
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                                        bytes.length);
                                if (recevedFrameOperation == ColorDetectionShield.RECEIVED_FRAMES.CENTER) {
                                    previewCells = new int[1];
                                    currentColorIndex = 0;
                                    addColorCell(1, 1);
                                } else {
                                    currentColorIndex = 0;
                                    previewCells = new int[9];
                                    for (int i = 0; i < 3; i++)
                                        for (int j = 0; j < 3; j++) {
                                            addColorCell(i, j);
                                            currentColorIndex += 1;
                                        }
                                }
                                if (colorDetectionMessenger != null && !isEqualColors()) {
                                    Bundle b = new Bundle();
                                    b.putIntArray("detected", previewCells);
                                    Message msg = Message.obtain(null, GET_RESULT);
                                    msg.setData(b);
                                    colorDetectionMessenger.send(msg);
                                    lastPreviewCells = previewCells;
                                }
                            } catch (Exception e) {
                                CrashlyticsUtils.logException(e);
                            }
                        }
                    }
                });
            }
        }
    };
    private boolean isBackPreview = true;
    private boolean isCapturing = false;
    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            Matrix bitmapMatrix = new Matrix();

            try {
                // Get the EXIF orientation.
                final Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(new ByteArrayInputStream(data)));
                final ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    final int exifOrientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                    switch (exifOrientation) {
                        case 1:
                            break;  // top left
                        case 2:
                            bitmapMatrix.postScale(-1, 1);
                            break;  // top right
                        case 3:
                            bitmapMatrix.postRotate(180);
                            break;  // bottom right
                        case 4:
                            bitmapMatrix.postRotate(180);
                            bitmapMatrix.postScale(-1, 1);
                            break;  // bottom left
                        case 5:
                            bitmapMatrix.postRotate(90);
                            bitmapMatrix.postScale(-1, 1);
                            break;  // left top
                        case 6:
                            bitmapMatrix.postRotate(90);
                            break;  // right top
                        case 7:
                            bitmapMatrix.postRotate(270);
                            bitmapMatrix.postScale(-1, 1);
                            break;  // right bottom
                        case 8:
                            bitmapMatrix.postRotate(270);
                            break;  // left bottom
                        default:
                            break;  // Unknown
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            Log.d("ImageTakin", "Done");
            if (bmp != null)
                bmp.recycle();
            // decode with options and set rotation
            bmp = ImageUtils.decodeBitmap(data, bitmapMatrix);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (bmp != null && QUALITY_MODE == 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
            else if (bmp != null && QUALITY_MODE != 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);

            File imagesFolder = new File(
                    Environment.getExternalStorageDirectory(), "OneSheeld");
            if (!imagesFolder.exists())
                imagesFolder.mkdirs(); // <----
            File image = new File(imagesFolder, System.currentTimeMillis()
                    + ".jpg");

            // write the bytes in file
            try {
                fo = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                Log.e("TAG", "FileNotFoundException", e);
                // TODO Auto-generated catch block
            }
            try {
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                Log.e("TAG", "fo.write::PictureTaken", e);
                // TODO Auto-generated catch block
            }
            // remember close de FileOutput
            try {
                fo.close();
                if (Build.VERSION.SDK_INT < 19)
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                else {
                    MediaScannerConnection
                            .scanFile(
                                    getApplicationContext(),
                                    new String[]{image.toString()},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(
                                                String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned "
                                                    + path + ":");
                                            Log.i("ExternalStorage", "-> uri="
                                                    + uri);
                                        }
                                    });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            com.integreight.onesheeld.utils.Log.d("Camera", "Image Taken !");
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.camera_your_picture_has_been_taken_toast, Toast.LENGTH_SHORT)
                            .show();
                }
            });
            resetPreview(registeredShieldsIDs == null || !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()));
            takenSuccessfully = true;
            updateLastImage(image.getAbsolutePath());
            notifyFinished();
            return;
        }
    };
    private final Messenger mMesesenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.replyTo != null && msg.what == GET_RESULT) {
                colorDetectionMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.COLOR_DETECTION_SHIELD.name());
                resetPreview(registeredShieldsIDs == null || !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()));
                notifyPreviewTypeChanged(isBackPreview, false);
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_OPERATION) {
                recevedFrameOperation = ColorDetectionShield.RECEIVED_FRAMES.getEnum(msg.getData().getInt("type"));
            } else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_TYPE) {
                colorType = ColorDetectionShield.COLOR_TYPE.getEnum(msg.getData().getInt("type"));
            } else if (msg.what == ColorDetectionShield.SET_COLOR_PATCH_SIZE) {
                cellSize = msg.getData().getInt("size");
            } else if (msg.what == ColorDetectionShield.UNBIND_COLOR_DETECTOR) {
                unBindColorDetector();
            } else if (msg.what == CameraShield.UNBIND_CAMERA_CAPTURE) {
                unBindCameraCapture();
            } else if (msg.what == CameraShield.BIND_CAMERA_CAPTURE) {
                cameraMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.CAMERA_SHIELD.name());
                resetPreview(registeredShieldsIDs == null || !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()));
                notifyPreviewTypeChanged(isBackPreview, true);
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == CAPTURE_IMAGE) {
                isCapturing = true;
                takeImage(msg.getData());
            } else if (msg.what == SHOW_PREVIEW) {
                if (msg.getData() == null || msg.getData().get("x") == null)
                    showPreview();
                else
                    showPreview(msg.getData().getFloat("x"), msg.getData().getFloat("y"), msg.getData().getInt("w"), msg.getData().getInt("h"));
            } else if (msg.what == INVALIDATE_PREVIEW) {
                if (msg.getData() == null || msg.getData().get("x") == null)
                    invalidateView();
                else
                    invalidateView(msg.getData().getFloat("x"), msg.getData().getFloat("y"));
            } else if (msg.what == HIDE_PREVIEW) hidePreview();
            else if (msg.what == SET_CAMERA_PREVIEW_TYPE) {
                if (!isCapturing) {
                    if (msg.getData().getBoolean("isBack")) {
                        if (mCamera != null) {
                            queue.removeCallbacks(null);
                            mCamera.setPreviewCallback(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = Camera.open();
                        } else
                            mCamera = getCameraInstance();

                        try {
                            if (mCamera != null) {
//                                mCamera.setDisplayOrientation(90);
                                setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
                                if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                                    mCamera.setPreviewCallback(previewCallback);
                                mCamera.setPreviewDisplay(sv.getHolder());
                                currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                                try {
                                    mCamera.startPreview();
                                } catch (Exception e) {
                                }
                                isBackPreview = true;
                            }
                        } catch (IOException e1) {
                        }
                        notifyPreviewTypeChanged(isBackPreview, true);
                    } else {
                        mCamera = openFrontFacingCameraGingerbread();
                        if (mCamera != null) {
                            try {
                                mCamera.setPreviewDisplay(sv.getHolder());
                                currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                                try {
                                    mCamera.startPreview();
                                } catch (Exception e) {
                                }
                                isBackPreview = false;
                            } catch (IOException e) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.camera_your_device_doesnt_have_a_front_camera_toast,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                        notifyPreviewTypeChanged(isBackPreview, true);
                    }
                } else
                    notifyPreviewTypeChanged(isBackPreview, true);
            }
        }
    });

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        registeredShieldsIDs = new ArrayList<>();
    }

    private Camera openFrontFacingCameraGingerbread() {
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("cameraS",
                            "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
//        cam.setDisplayOrientation(90);
        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT, cam);
        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
            cam.setPreviewCallback(previewCallback);
        return cam;
    }

    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void setBesttPictureResolution() {
        // get biggest picture size
        width = pref.getInt("Picture_Width", 0);
        height = pref.getInt("Picture_height", 0);

        if (width == 0 | height == 0) {
            pictureSize = CameraUtils.getBiggestPictureSize(parameters);
            if (pictureSize != null)
                parameters
                        .setPictureSize(pictureSize.width, pictureSize.height);
            // save width and height in sharedprefrences
            width = pictureSize.width;
            height = pictureSize.height;
            editor.putInt("Picture_Width", width);
            editor.putInt("Picture_height", height);
            editor.commit();

        } else {
            // if (pictureSize != null)
            parameters.setPictureSize(width, height);
        }
    }

    private void showPreview() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int expectedHeight = metrics.heightPixels - ((int) (280 * metrics.density + .5f));
        int expectedWidth = ((expectedHeight * (size == null ? metrics.widthPixels : size.height)) / (size == null ? metrics.heightPixels : size.width));
        params.x = ((metrics.widthPixels / 2) - expectedWidth / 2);
        params.y = (int) (150 * metrics.density + .5f);
        params.width = expectedWidth;
        params.height = expectedHeight;
        params.alpha = 1.0f;
        try {
            windowManager.updateViewLayout(sv, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private void showPreview(float x, float y, int w, int h) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        params.width = w;
        params.height = h + ((int) (1 * getResources().getDisplayMetrics().density + .5f));
        params.x = (int) x;
        params.y = (int) (y);
        params.alpha = 1.0f;
        try {
            windowManager.updateViewLayout(sv, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private void invalidateView() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int expectedHeight = metrics.heightPixels - ((int) (280 * metrics.density + .5f));
        int expectedWidth = ((expectedHeight * (size == null ? metrics.widthPixels : size.height)) / (size == null ? metrics.heightPixels : size.width));
        params.x = ((metrics.widthPixels / 2) - expectedWidth / 2);
        params.y = (int) (150 * metrics.density + .5f);
        params.width = 1;
        params.height = 1;
        params.alpha = 1;
        try {
            windowManager.updateViewLayout(sv, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private void invalidateView(float x, float y) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        params.width = 1;
        params.height = 1;
        params.x = (int) x;
        params.y = (int) (y);
        params.alpha = 1;
        try {
            windowManager.updateViewLayout(sv, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private void hidePreview() {
        params.width = 1;
        params.height = 1;
        params.alpha = 1;
        try {
            windowManager.updateViewLayout(sv, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private synchronized void takeImage(Bundle extras) {

        if (CameraUtils.checkCameraHardware(getApplicationContext())) {
            takenSuccessfully = false;
            isCapturing = true;
            if (extras != null) {
                String flash_mode = extras.getString("FLASH");
                FLASH_MODE = flash_mode;

                boolean front_cam_req = extras.getBoolean("Front_Request");
                isFrontCamRequest = front_cam_req;

                int quality_mode = extras.getInt("Quality_Mode");
                QUALITY_MODE = quality_mode;
            }

            if (isFrontCamRequest) {
                // set flash 0ff
                FLASH_MODE = "off";
                // only for gingerbread and newer versions
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {

                    mCamera = openFrontFacingCameraGingerbread();
                    if (mCamera != null) {

                        try {
                            mCamera.setPreviewDisplay(sv.getHolder());
                            currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                            notifyPreviewTypeChanged(false, true);
                        } catch (IOException e) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.camera_your_device_doesnt_have_a_front_camera_toast,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            takenSuccessfully = false;
                            notifyFinished();
                            return;
                        }
                        Camera.Parameters parameters = mCamera.getParameters();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
//                                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                        }
//                        else
                        if (parameters.getSupportedFocusModes().contains(
                                Camera.Parameters.FOCUS_MODE_AUTO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        pictureSize = CameraUtils
                                .getBiggestPictureSize(parameters);
                        if (pictureSize != null)
                            parameters.setPictureSize(pictureSize.width,
                                    pictureSize.height);


                        android.hardware.Camera.CameraInfo info =
                                new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
                        int mRotation = (info.orientation - mOrientation + 360) % 360;
                        parameters.setRotation(mRotation);

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                            mCamera.setPreviewCallback(previewCallback);
                        takePictureWithAutoFocus(mCamera, mCall);

                        // return 4;

                    } else {
                        mCamera = null;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(
                                        getApplicationContext(),
                                        R.string.camera_your_device_doesnt_have_a_front_camera_toast,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        takenSuccessfully = false;


                        notifyFinished();
                        return;
                    }

                } else {
                    if (CameraUtils.checkFrontCamera(getApplicationContext())) {
                        mCamera = openFrontFacingCameraGingerbread();

                        if (mCamera != null) {
                            try {
                                mCamera.setPreviewDisplay(sv.getHolder());
                                currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                                notifyPreviewTypeChanged(false, true);
                            } catch (IOException e) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                R.string.camera_your_device_doesnt_have_a_front_camera_toast,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                takenSuccessfully = false;
                                notifyFinished();
                                return;
                            }
                            Camera.Parameters parameters = mCamera
                                    .getParameters();
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
//                                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                            }
//                            else
                            if (parameters.getSupportedFocusModes().contains(
                                    Camera.Parameters.FOCUS_MODE_AUTO)) {
                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            }
                            pictureSize = CameraUtils
                                    .getBiggestPictureSize(parameters);
                            if (pictureSize != null)
                                parameters.setPictureSize(pictureSize.width,
                                        pictureSize.height);

                            android.hardware.Camera.CameraInfo info =
                                    new android.hardware.Camera.CameraInfo();
                            android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
                            int mRotation = (info.orientation - mOrientation + 360) % 360;
                            parameters.setRotation(mRotation);

                            // set camera parameters
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                            if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                                mCamera.setPreviewCallback(previewCallback);


                            takePictureWithAutoFocus(mCamera, mCall);
                            // return 4;

                        } else {
                            mCamera = null;
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            R.string.camera_your_device_doesnt_have_a_front_camera_toast,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            takenSuccessfully = false;
                            notifyFinished();
                            return;
                        }

                    }

                }

            } else {
                if (mCamera != null) {
                    queue.removeCallbacks(null);
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = Camera.open();
                } else
                    mCamera = getCameraInstance();

                try {
                    if (mCamera != null) {
//                        mCamera.setDisplayOrientation(90);
                        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
                        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                            mCamera.setPreviewCallback(previewCallback);
                        mCamera.setPreviewDisplay(sv.getHolder());
                        currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                        notifyPreviewTypeChanged(true, true);
                        parameters = mCamera.getParameters();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
//                                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                        }
//                        else
                        if (parameters.getSupportedFocusModes().contains(
                                Camera.Parameters.FOCUS_MODE_AUTO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                            FLASH_MODE = "auto";
                        }
                        if (CameraUtils.hasFlash(mCamera)) parameters.setFlashMode(FLASH_MODE);
                        // set biggest picture
                        setBesttPictureResolution();
                        // log quality and image format
                        Log.d("Quality", parameters.getJpegQuality() + "");
                        Log.d("Format", parameters.getPictureFormat() + "");

                        android.hardware.Camera.CameraInfo info =
                                new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                        int mRotation = (info.orientation + mOrientation) % 360;
                        parameters.setRotation(mRotation);

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        Log.d("cameraS", "OnTake()");
                        takePictureWithAutoFocus(mCamera, mCall);
                    } else {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        R.string.camera_camera_is_not_available_toast,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        takenSuccessfully = false;
                        notifyFinished();
                        return;
                    }
                    // return 4;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "CmaraHeadService()::takePicture", e);
                    takenSuccessfully = false;
                    notifyFinished();
                }

            }

        } else {

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.camera_your_device_doesnt_have_a_camera_toast,
                            Toast.LENGTH_LONG).show();
                }
            });
            takenSuccessfully = false;
            notifyFinished();
            return;
        }

        // return super.onStartCommand(intent, flags, startId);

    }

    private void takePictureWithAutoFocus(final android.hardware.Camera mCamera, final Camera.PictureCallback mCall) {
        final Handler uiThreadHandler = new Handler();
        if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    uiThreadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCamera.takePicture(new Camera.ShutterCallback() {
                                @Override
                                public void onShutter() {
                                    mCamera.cancelAutoFocus();
                                }
                            }, null, mCall);
                        }
                    }, 500);
                }
            });
        } else {
            uiThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCamera.takePicture(null, null, mCall);
                }
            }, 500);
        }
    }

    private void start(final boolean isCamera) {
        Log.d("ImageTakin", "StartCommand()");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = Camera.open();
        } else
            mCamera = getCameraInstance();
        parameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        size = parameters.getPreviewSize();
        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
            mCamera.setPreviewCallback(previewCallback);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBX_8888);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int expectedHeight = metrics.heightPixels - ((int) (250 * metrics.density + .5f));
        int expectedWidth = ((expectedHeight * (size == null ? metrics.widthPixels : size.height)) / (size == null ? metrics.heightPixels : size.width));
        params.width = 1;// metrics.widthPixels - ((int) (60 * metrics.density + .5f));
        params.height = 1;
        params.x = (int) ((metrics.widthPixels / 2) - expectedWidth / 2);
        params.y = (int) (150 * metrics.density + .5f);
        params.alpha = 1;
        sv = new SurfaceView(getApplicationContext());
        windowManager.addView(sv, params);
        params = (WindowManager.LayoutParams) sv.getLayoutParams();
        sHolder = sv.getHolder();
        sHolder.addCallback(this);

        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) return;

                mOrientation = (orientation + 45) / 90 * 90;
            }
        };
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
        // tells Android that this surface will have its data constantly
        // replaced
        if (Build.VERSION.SDK_INT < 11)
            sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // sv = new SurfaceView(getApplicationContext());
        if (sv == null)
            start(intent != null && intent.getExtras() != null && intent.getBooleanExtra("isCamera", false));
        registeredShieldsIDs = new ArrayList<>();
        return 1;
    }

    private void initCrashlyticsAndUncaughtThreadHandler() {
        Thread.UncaughtExceptionHandler myHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, final Throwable arg1) {
                if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()) && colorDetectionMessenger != null) {
                    try {
                        Message msg = Message.obtain(null, CRASHED);
                        colorDetectionMessenger.send(msg);
                    } catch (RemoteException e) {
                    }
                    if (!registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
                        android.os.Process.killProcess(android.os.Process.myPid());

                }
                if (registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name())) {
                    try {
                        Message msg = Message.obtain(null, CameraShield.CRASHED);
                        cameraMessenger.send(msg);
                    } catch (RemoteException e) {
                    }
                    if (!registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                        android.os.Process.killProcess(android.os.Process.myPid());
                }
                if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()) && registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name())) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        if (registeredShieldsIDs != null)
            registeredShieldsIDs.clear();
        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLastImage(String path) {
        Bundle intent = new Bundle();
        intent.putString("absolutePath", path);
        try {
            Message msg = Message.obtain(null, CameraShield.SET_LAST_IMAGE_BUTTON);
            msg.setData(intent);
            if (cameraMessenger != null)
                cameraMessenger.send(msg);
        } catch (RemoteException e) {
        }
    }

    private void notifyFinished() {
        isCapturing = false;
        Bundle intent = new Bundle();
        intent.putBoolean("takenSuccessfuly", takenSuccessfully);
        try {
            Message msg = Message.obtain(null, CameraShield.NEXT_CAPTURE);
            msg.setData(intent);
            if (cameraMessenger != null)
                cameraMessenger.send(msg);
        } catch (RemoteException e) {
        }
        resetPreview(true);
    }

    private void notifyPreviewTypeChanged(boolean isBack, boolean forCamerShield) {
        isBackPreview = isBack;
        Bundle intent = new Bundle();
        intent.putBoolean("isBack", isBack);
        try {
            Message msg = Message.obtain(null, SET_CAMERA_PREVIEW_TYPE);
            msg.setData(intent);
            if (forCamerShield) {
                if (cameraMessenger != null)
                    cameraMessenger.send(msg);
            } else {
                if (colorDetectionMessenger != null)
                    colorDetectionMessenger.send(msg);
            }
        } catch (RemoteException e) {
        }
        if (forCamerShield)
            notifyPreviewTypeChanged(isBack, false);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (registeredShieldsIDs.size() == 0) {
            stopSelf();
            Log.d("dCamera", "unboundHead");
        }
        registeredShieldsIDs.clear();
        return super.onUnbind(intent);
    }

    public void unBindColorDetector() {
        if (mCamera != null) {
            try {
                if (sv != null && registeredShieldsIDs.size() == 1) {
                    if (mCamera != null)
                        mCamera.setPreviewCallback(null);
                    windowManager.removeView(sv);
                }
            } catch (Exception e) {

            }
            if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                registeredShieldsIDs.remove(UIShield.COLOR_DETECTION_SHIELD.name());
            Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            colorDetectionMessenger = null;
        }
    }

    public void unBindCameraCapture() {
        if (mCamera != null) {
            try {
                if (sv != null && registeredShieldsIDs.size() == 1) {
                    if (mCamera != null)
                        mCamera.setPreviewCallback(null);
                    windowManager.removeView(sv);
                }
            } catch (Exception e) {

            }
        }
        if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
            registeredShieldsIDs.remove(UIShield.CAMERA_SHIELD.name());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("dCamera", "boundHead");
        initCrashlyticsAndUncaughtThreadHandler();
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
        start(intent.getBooleanExtra("isCamera", false));
        return mMesesenger.getBinder();
    }

    private void resetPreview(boolean isCamera) {
        if (sHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        if (mCamera == null)
            start(isCamera);
        else {
            try {
                mCamera.setPreviewDisplay(sHolder);
                try {
                    setCameraDisplayOrientation(currentCamera, mCamera);
                } catch (RuntimeException e) {
                }
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                    mCamera.setPreviewCallback(previewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            if (queue != null)
                queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        try {
            if (sv != null)
                windowManager.removeView(sv);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        notifyFinished();
        android.os.Process.killProcess(android.os.Process.myPid());

        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        resetPreview(registeredShieldsIDs == null || !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resetPreview(registeredShieldsIDs == null || !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            try {
                if (sv != null)
                    windowManager.removeView(sv);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isEqualColors() {
        if (previewCells == null || lastPreviewCells == null)
            return false;
        if (previewCells.length == lastPreviewCells.length) {
            for (int i = 0; i < previewCells.length; i++) {
                if (previewCells[i] != lastPreviewCells[i])
                    return false;
            }
            return true;
        } else
            return false;
    }
//    int vv = 0;

    private void addColorCell(int i, int j) {
        if (bitmap != null) {
            w = (bitmap.getWidth() / 3) / cellSize;
            h = (bitmap.getHeight() / 3) / cellSize;
            x = ((bitmap.getWidth() / 2) - (w / 2)) * i;
            y = ((bitmap.getHeight() / 2) - (h / 2)) * (2 - j);
            if (bitmap.getWidth() - x < w) w = bitmap.getWidth() - x;
            if (bitmap.getHeight() - y < h) h = bitmap.getHeight() - y;
            resizebitmap = Bitmap.createBitmap(bitmap, x, y, w, h, null, false);
            if (colorType == ColorDetectionShield.COLOR_TYPE.COMMON) {
                resizebitmap = Bitmap.createScaledBitmap(resizebitmap, 1, 1, true);
                int dominant = resizebitmap.getPixel(0, 0);
                int dominantColor = Color.rgb(Color.red(dominant), Color.green(dominant)
                        , Color.blue(dominant));
                previewCells[currentColorIndex] = dominantColor;
            } else {
                int average = ImageUtils.getAverageColor(resizebitmap);
                int averageColor = Color.rgb(Color.red(average), Color.green(average)
                        , Color.blue(average));
                previewCells[currentColorIndex] = averageColor;
            }
        }
    }
}
