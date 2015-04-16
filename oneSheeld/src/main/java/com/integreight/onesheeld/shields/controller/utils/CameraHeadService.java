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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    //    TextView tv, tvD;
//    ImageView iv;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    WindowManager.LayoutParams params;//, params1, params2, params3;
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

    private void showPreview() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        params.width = metrics.widthPixels - ((int) (60 * metrics.density + .5f));
        params.height = metrics.heightPixels - ((int) (250 * metrics.density + .5f));
//        sv.setLayoutParams(params);
//        sv.requestLayout();
        windowManager.updateViewLayout(sv, params);
    }

    private void hidePreview() {
        params.width = 0;
        params.height = 0;
        windowManager.updateViewLayout(sv, params);
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
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBX_8888);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        params.width = 100;
        params.height = 100;
        params.x = (int) (30 * metrics.density + .5f);
        params.y = (int) (150 * metrics.density + .5f);
        sv = new SurfaceView(getApplicationContext());
        windowManager.addView(sv, params);
        params = (WindowManager.LayoutParams) sv.getLayoutParams();
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
            } else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_OPERATION) {
                recevedFrameOperation = ColorDetectionShield.RECEIVED_FRAMES.getEnum(msg.getData().getInt("type"));
            } else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_TYPE) {
                colorType = ColorDetectionShield.COLOR_TYPE.getEnum(msg.getData().getInt("type"));
            } else if (msg.what == ColorDetectionShield.SET_COLOR_PATCH_SIZE) {
                cellSize = msg.getData().getInt("size");
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
            } else if (msg.what == CameraShield.SHOW_PREVIEW) showPreview();
            else if (msg.what == CameraShield.HIDE_PREVIEW) hidePreview();
        }
    });

    public Messenger colorDetectionMessenger;

    @Override
    public boolean onUnbind(Intent intent) {
//        if (registeredShieldsIDs.size() == 0) {
        stopSelf();
        Log.d("dCamera", "unboundHead");
//        }
        return super.onUnbind(intent);
    }

    public void unBindColorDetector() {
        if (mCamera != null) {
            try {
                if (sv != null && registeredShieldsIDs.size() == 1) {
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
                    mCamera.setPreviewCallback(null);
                    windowManager.removeView(sv);
                }
            } catch (Exception e) {

            }
        }
        if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
            registeredShieldsIDs.remove(UIShield.CAMERA_SHIELD.name());
    }

    //    public void recursiveTouch(final View v) {
//        if (v instanceof ViewGroup) {
//            ViewGroup vg = (ViewGroup) v;
//            if (vg.findViewById(R.id.camera_preview) != null) {
//                sv = (SurfaceView) vg.findViewById(R.id.camera_preview);
//                sHolder = sv.getHolder();
//                sHolder.addCallback(CameraHeadService.this);
//                sv.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mCamera != null)
//                            try {
//                                mCamera.setPreviewDisplay(sHolder);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                    }
//                });
//                Log.d("dones", sHolder.toString());
//                return;
//            }
//            for (int i = 0; i < vg.getChildCount(); i++) {
//                recursiveTouch(vg.getChildAt(i));
//            }
//        }
//
//    }
//
//    private void resetSurfaceView() {
//        try {
//            Class c = Class.forName("android.view.WindowManagerGlobal");
//            // Field[] fs = c.getDeclaredFields();
//            // for (int i = 0; i < fs.length; i++) {
//            // System.out.println("@#@#   " + i + "   " + fs[i].getName());
//            // }
//            Method m = c.getMethod("getInstance", null);
//            Object o = m.invoke(c, null);
//
//            Field f = c.getDeclaredField("mViews");
//            f.setAccessible(true);
//            final View[] views = (View[]) f.get(o);
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    while (true) {
//                        for (View view : views) {
//                            try {
//                                if (view.getParent() != null)
//                                    Log.d("d", (view.getParent() instanceof ViewGroup) + "");
////                                    recursiveTouch((ViewGroup) (view.getParent().getParent() != null ?
////                                            view.getParent().getParent() : view.getParent()));
//                            } catch (ClassCastException e) {
//                            }
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                            break;
//                        }
//                    }
//                }
//            }).start();
//            System.out.println("@#@#  "
//                    + ((ViewGroup) ((ViewGroup) views[0]).getChildAt(0))
//                    .getChildCount());
//            // Method[] methods = c.getMethods();
//            // for (int i = 0; i < methods.length; i++) {
//            // Method m1 = methods[i];
//            // m1.setAccessible(true);
//            // System.out.println("public method: " + m1.getName() + "    "
//            // + c.getModifiers());
//            // }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("dCamera", "boundHead");
        initCrashlyticsAndUncaughtThreadHandler();
        Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
        start(intent.getBooleanExtra("isCamera", false));
//        resetSurfaceView();
        return mMesesenger.getBinder();
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
        } catch (Exception e) {

        }
        isRunning = false;
        android.os.Process.killProcess(android.os.Process.myPid());
        Intent intent = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
        intent.putExtra("takenSuccessfuly", takenSuccessfully);
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
        }
    }

    int[] previewCells;
    int currentColorIndex = 0;
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
                                if (colorDetectionMessenger != null) {
                                    Bundle b = new Bundle();
                                    b.putIntArray("detected", previewCells);
                                    Message msg = Message.obtain(null, GET_RESULT);
                                    msg.setData(b);
                                    colorDetectionMessenger.send(msg);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                });
            }
        }
    };
    private final int quality = 50;
    int w = 0;
    int h = 0;
    int x = 0;
    int y = 0;
    private int cellSize = 1;

    private void addColorCell(int i, int j) {
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
