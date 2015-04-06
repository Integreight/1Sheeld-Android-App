package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
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
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class CameraHeadService extends Service implements
        SurfaceHolder.Callback {

    // Camera variables
    // a surface holder
    // a variable to control the camera
    private Camera mCamera;
    // the camera parametersrivate Bitmap bmp;
    FileOutputStream fo;
    private Bitmap bmp;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0;
    private boolean isFrontCamRequest = false;
    private Camera.Size pictureSize;
    SurfaceView sv;
    TextView tv, tvD;
    ImageView iv;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    WindowManager.LayoutParams params, params1, params2, params3;
    SharedPreferences pref;
    Editor editor;
    int width = 0, height = 0;
    private ArrayList<String> registeredShieldsIDs = new ArrayList<>();

    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = -1;
    public static boolean isRunning = false;
    private boolean takenSuccessfully = false;
    public static final int GET_RESULT = 1, CAPTURE_IMAGE = 3;
    private static Handler queue = new Handler();
    private byte[] data;
    ByteArrayOutputStream out;
    Camera.Parameters parameters;
    Camera.Size size;
    YuvImage yuv;
    byte[] bytes;
    Bitmap bitmap;
    Bitmap resizebitmap;

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
        cam.setDisplayOrientation(90);
        return cam;
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

    Handler handler = new Handler();

    private synchronized void takeImage(Bundle extras) {

        if (CameraUtils.checkCameraHardware(getApplicationContext())) {
            takenSuccessfully = false;
            isRunning = true;
//            Bundle extras = intent.getExtras();
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
                        } catch (IOException e) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "API dosen't support front camera",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            takenSuccessfully = false;
                            notifyFinished();
                            return;
                        }
                        Camera.Parameters parameters = mCamera.getParameters();
                        pictureSize = CameraUtils
                                .getBiggestPictureSize(parameters);
                        if (pictureSize != null)
                            parameters.setPictureSize(pictureSize.width,
                                    pictureSize.height);

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        mCamera.setPreviewCallback(previewCallback);
                        mCamera.takePicture(null, null, mCall);

                        // return 4;

                    } else {
                        mCamera = null;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Your device doesn't have a front camera!",
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
                            } catch (IOException e) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "API doesn't support front camera",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                takenSuccessfully = false;
                                notifyFinished();
                                return;
                            }
                            Camera.Parameters parameters = mCamera
                                    .getParameters();
                            pictureSize = CameraUtils
                                    .getBiggestPictureSize(parameters);
                            if (pictureSize != null)
                                parameters.setPictureSize(pictureSize.width,
                                        pictureSize.height);

                            // set camera parameters
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                            mCamera.setPreviewCallback(previewCallback);
                            mCamera.takePicture(null, null, mCall);
                            // return 4;

                        } else {
                            mCamera = null;
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Your device doesn't have a front camera!",
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
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewCallback(previewCallback);
                        mCamera.setPreviewDisplay(sv.getHolder());
                        parameters = mCamera.getParameters();
                        if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                            FLASH_MODE = "auto";
                        }
                        if (CameraUtils.hasFlash(mCamera)) parameters.setFlashMode(FLASH_MODE);
                        // set biggest picture
                        setBesttPictureResolution();
                        // log quality and image format
                        Log.d("Quality", parameters.getJpegQuality() + "");
                        Log.d("Format", parameters.getPictureFormat() + "");

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        Log.d("cameraS", "OnTake()");
                        mCamera.takePicture(null, null, mCall);
                    } else {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Camera is unavailable !",
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
                }

            }

        } else {

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Your device doesn't have a camera!",
                            Toast.LENGTH_LONG).show();
                }
            });
            takenSuccessfully = false;
            notifyFinished();
            return;
        }

        // return super.onStartCommand(intent, flags, startId);

    }

    private void start(final boolean isCamera) {
//        isRunning = true;
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
        mCamera.setPreviewCallback(previewCallback);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBX_8888);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 200;
        params.height = 200;
        params.x = 0;
        params.y = 0;
        sv = new SurfaceView(getApplicationContext());
        tv = new TextView(getApplicationContext());
        tvD = new TextView(getApplicationContext());
        iv = new ImageView(getApplicationContext());
        windowManager.addView(sv, params);
        params1 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBX_8888);

        params1.gravity = Gravity.TOP | Gravity.LEFT;
        params1.width = 200;
        params1.height = 200;
        params1.x = 0;
        params1.y = 200;
        windowManager.addView(tv, params1);
        params2 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBX_8888);

        params2.gravity = Gravity.TOP | Gravity.LEFT;
        params2.width = 200;
        params2.height = 200;
        params2.x = 0;
        params2.y = 400;
        windowManager.addView(tvD, params2);
        params3 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBX_8888);
        params3.gravity = Gravity.TOP | Gravity.LEFT;
        params3.width = 200;
        params3.height = 200;
        params3.x = 0;
        params3.y = 600;
        windowManager.addView(iv, params3);
        sHolder = sv.getHolder();
        sHolder.addCallback(this);

        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                        .getDefaultDisplay();
                int rotation = windowManager.getDefaultDisplay().getRotation();
                Log.sysOut(rotation + "");

                if (display.getRotation() != Surface.ROTATION_0) { // landscape
                    // oriented
                    // devices
                    Log.sysOut("LANDSCAPE");
                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    } else if (orientation < 135 && orientation > 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    }
                } else { // portrait oriented devices
                    Log.sysOut("PORTRAIT");
                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else if (orientation < 135 && orientation > 45) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }
                }

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
        start(intent != null && intent.getExtras() != null && intent.getBooleanExtra("isCamera", false));
        registeredShieldsIDs = new ArrayList<>();
        return 1;
    }

    private void initCrashlyticsAndUncaughtThreadHandler() {
        Thread.UncaughtExceptionHandler myHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, final Throwable arg1) {
                Intent intent = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
                intent.putExtra("crashed", true);
                isRunning = false;
                LocalBroadcastManager.getInstance(CameraHeadService.this).sendBroadcast(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        if (MainActivity.hasCrashlyticsApiKey(this)) {
            Crashlytics.start(this);
        }
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            int rotate = 0;
            switch (mOrientation) {
                case ORIENTATION_PORTRAIT_NORMAL:
                    if (!isFrontCamRequest) rotate = 90;
                    else rotate = 270;
                    break;
                case ORIENTATION_LANDSCAPE_NORMAL:
                    rotate = 0;
                    break;
                case ORIENTATION_PORTRAIT_INVERTED:
                    if (!isFrontCamRequest) rotate = 270;
                    else rotate = 90;
                    break;
                case ORIENTATION_LANDSCAPE_INVERTED:
                    rotate = 180;
                    break;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);

            Log.d("ImageTakin", "Done");
            if (bmp != null)
                bmp.recycle();
            // decode with options and set rotation
            bmp = ImageUtils.decodeBitmap(data, matrix);
            data = null;

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
//            if (mCamera != null) {
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
//            }
            com.integreight.onesheeld.utils.Log.d("Camera", "Image Taken !");
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
//            mCamera = null;
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Your Picture has been taken !", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            resetPreview(0, 0);
            takenSuccessfully = true;
            notifyFinished();
            return;
        }
    };

    private void notifyFinished() {
        Intent intent = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
        intent.putExtra("takenSuccessfuly", takenSuccessfully);
        isRunning = false;
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private final Messenger mMesesenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.replyTo != null && msg.what == GET_RESULT) {
                colorDetectionMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.COLOR_DETECTION_SHIELD.name());
                resetPreview(0, 0);
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == ColorDetectionShield.UNBIND_COLOR_DETECTOR) {
                unBindColorDetector();
            } else if (msg.what == CameraAidlService.UNBIND_CAMERA_CAPTURE) {
                unBindCameraCapture();
            } else if (msg.what == CameraAidlService.BIND_CAMERA_CAPTURE) {
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.CAMERA_SHIELD.name());
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == CAPTURE_IMAGE) {
                isRunning = true;
                takeImage(msg.getData());
            }
        }
    });

    public Messenger colorDetectionMessenger;

    @Override
    public boolean onUnbind(Intent intent) {
        if (registeredShieldsIDs.size() == 0) {
            stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onUnbind(intent);
    }

    public void unBindColorDetector() {
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
        if (mCamera != null) {
            try {
                if (sv != null && registeredShieldsIDs.size() == 1) {
                    mCamera.setPreviewCallback(null);
                    windowManager.removeView(sv);
                }
                if (tv != null)
                    windowManager.removeView(tv);
                if (tvD != null)
                    windowManager.removeView(tvD);
                if (iv != null)
                    windowManager.removeView(iv);
            } catch (Exception e) {

            }
            if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                registeredShieldsIDs.remove(UIShield.COLOR_DETECTION_SHIELD.name());
            Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            colorDetectionMessenger = null;
//            if (registeredShieldsIDs.size() == 0) {
//                stopSelf();
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }
        }
    }

    public void unBindCameraCapture() {
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
        if (mCamera != null) {
            try {
                if (sv != null && registeredShieldsIDs.size() == 1) {
                    mCamera.setPreviewCallback(null);
                    windowManager.removeView(sv);
                }
                if (tv != null && registeredShieldsIDs.size() == 1)
                    windowManager.removeView(tv);
                if (tvD != null && registeredShieldsIDs.size() == 1)
                    windowManager.removeView(tvD);
                if (iv != null && registeredShieldsIDs.size() == 1)
                    windowManager.removeView(iv);
            } catch (Exception e) {

            }
        }
        if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
            registeredShieldsIDs.remove(UIShield.CAMERA_SHIELD.name());
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
//        if (registeredShieldsIDs.size() == 0) {
//            stopSelf();
//            android.os.Process.killProcess(android.os.Process.myPid());
//        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        initCrashlyticsAndUncaughtThreadHandler();
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
//        if (registeredShieldsIDs.size() == 0)
        start(intent.getBooleanExtra("isCamera", false));
//        if (intent.getBooleanExtra("isCamera", false)) {
//            return mMesesenger.getBinder();
//        } else {
        return mMesesenger.getBinder();
//        }
    }

    private void resetPreview(int w, int h) {
        if (sHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            if (w > 0 && h > 0) {
                Camera.Parameters parameters1 = mCamera.getParameters();
                parameters1.setPreviewSize(w, h);
            }
            mCamera.setPreviewDisplay(sHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewCallback(previewCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        try {
            if (sv != null)
                windowManager.removeView(sv);
            if (tv != null)
                windowManager.removeView(tv);
            if (tvD != null)
                windowManager.removeView(tvD);
            if (iv != null)
                windowManager.removeView(iv);
        } catch (Exception e) {

        }
        Intent intent = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
        intent.putExtra("takenSuccessfuly", takenSuccessfully);
        isRunning = false;
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        resetPreview(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        if (cameraIntent != null)
//            takeImage(cameraIntent);
        resetPreview(0, 0);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
//            if (sv != null)
//                windowManager.removeView(sv);
//            if (tv != null)
//                windowManager.removeView(tv);
//            if (tvD != null)
//                windowManager.removeView(tvD);
//            if (iv != null)
//                windowManager.removeView(iv);
        }
    }

    ArrayList<PreviewCell> previewCells = new ArrayList<>();
    private ColorDetectionShield.RECEIVED_FRAMES recevedFrame = ColorDetectionShield.RECEIVED_FRAMES.CENTER;
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
                                previewCells = new ArrayList<>();
                                if (recevedFrame == ColorDetectionShield.RECEIVED_FRAMES.CENTER) {
                                    addColorCell(data, camera, 1, 1);
                                } else {
                                    for (int i = 0; i < 3; i++)
                                        for (int j = 0; j < 3; j++) {
                                            addColorCell(data, camera, i, j);
                                        }
                                }
                                if (colorDetectionMessenger != null) {
                                    Bundle b = new Bundle();
                                    b.putSerializable("detected", previewCells);
                                    Message msg = Message.obtain(null, GET_RESULT);
                                    msg.setData(b);
                                    mMesesenger.send(msg);
                                }
//                                if (resizebitmap != null && !resizebitmap.isRecycled())
//                                    resizebitmap.recycle();
//                                resizebitmap = null;
                                // send frame of most common color to arduino
//                                            sendColorToArduino(commonColor, dominanteColor);

                                //
                            } catch (Exception e) {
                            }
                        }
                    }
                });
            }
        }
    };

    private void addColorCell(byte[] data1, final Camera camera, int i, int j) {
        out = new ByteArrayOutputStream();
        parameters = camera.getParameters();
        size = parameters.getPreviewSize();
        yuv = new YuvImage(data, ImageFormat.NV21, size.width,
                size.height, null);

        // bWidth and bHeight define the size of the bitmap you wish the
        // fill with the preview image
        yuv.compressToJpeg(new Rect(0, 0, size.width, size.height),
                100, out);
        bytes = out.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                bytes.length);
        resizebitmap = Bitmap.createBitmap(bitmap,
                ((bitmap.getWidth() / 2) - 25) * i, ((bitmap.getHeight() / 2) - 25) * j, 50, 50);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        resizebitmap = Bitmap.createBitmap(resizebitmap, 0, 0, resizebitmap.getWidth(), resizebitmap.getHeight(), matrix, true);
        iv.setImageBitmap(resizebitmap);
        int commonColor = ImageUtils.getMostDominantColor(resizebitmap);
        int common = Color.rgb(Color.red(commonColor), Color.green(commonColor)
                , Color.blue(commonColor));
        tv.setBackgroundColor(common);
        int average = ImageUtils.getAverageColor(resizebitmap);
        int averageColor = Color.rgb(Color.red(average), Color.green(average)
                , Color.blue(average));
        tvD.setBackgroundColor(averageColor);
        previewCells.add(new PreviewCell(commonColor, average));
    }

    public static class PreviewCell implements Serializable {
        public int common, average;

        public PreviewCell(int common, int average) {
            this.common = common;
            this.average = average;
        }
    }
}
