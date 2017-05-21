package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
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
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.integreight.onesheeld.R;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.utils.FaceGraphic;
import com.integreight.onesheeld.utils.customviews.utils.GraphicOverlay;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;

import static com.integreight.onesheeld.shields.controller.FaceDetectionShield.IS_FACE_ACTIVE;
import static com.integreight.onesheeld.shields.controller.FaceDetectionShield.SEND_EMPTY;
import static com.integreight.onesheeld.shields.controller.FaceDetectionShield.SEND_FACES;
import static com.integreight.onesheeld.shields.controller.FaceDetectionShield.START_DETECTION;

public class CameraHeadService extends Service implements
        SurfaceHolder.Callback {

    public static final int GET_RESULT = 1, CAPTURE_IMAGE = 3, CRASHED = 13, SET_CAMERA_PREVIEW_TYPE = 15;
    public final static int SHOW_PREVIEW = 7;
    public final static int HIDE_PREVIEW = 8;
    public final static int INVALIDATE_PREVIEW = 16;
    private static final String TAG = CameraHeadService.class.getSimpleName();
    private static Handler queue = new Handler();
    /**
     * Map to convert between a byte array, received from the camera, and its associated byte
     * buffer.  We use byte buffers internally because this is a more efficient way to call into
     * native code later (avoids a potential copy).
     */
    private Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();
    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    private Thread mProcessingThread;
    private FrameProcessingRunnable mFrameProcessor;
    FaceDetector detectorVertical;
    FaceDetector detectorHorizontal;
    int previewHeight;
    int previewWidth;
    private List<GraphicOverlay.Graphic> graphicsList = null;
    private FaceGraphic mFaceGraphic;
    private GraphicOverlay mGraphicOverlay;
    private Messenger faceDetectionMessenger;
    private int dispAngle;
    FrameLayout fLayout;
    private final int quality = 50;
    FileOutputStream fo;
    SurfaceView sv;
    WindowManager.LayoutParams params;
    SharedPreferences pref;
    Editor editor;
    // Four frame buffers are needed for working with the camera:
    //
    //   one for the frame that is currently being executed upon in doing detection
    //   one for the next pending frame to process immediately upon completing detection
    //   two for the frames that the camera uses to populate future preview images
    byte[] frame1, frame2, frame3, frame4;
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
    private int faceRotation;
    private boolean takenSuccessfully = false;
    private Messenger colorDetectionMessenger;
    private Messenger cameraMessenger;
    private int cellSize = 1;
    private int currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
    private ColorDetectionShield.RECEIVED_FRAMES recevedFrameOperation = ColorDetectionShield.RECEIVED_FRAMES.CENTER;
    private ColorDetectionShield.COLOR_TYPE colorType = ColorDetectionShield.COLOR_TYPE.COMMON;
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public synchronized void onPreviewFrame(byte[] data, Camera camera) {
            if (colorDetectionMessenger != null)
                colorDetectionFramesProcessing(data, camera);
            if (faceDetectionMessenger != null)
                faceDetectionFramesProcessing(data, camera);
        }
    };

    private void faceDetectionFramesProcessing(final byte[] data, final Camera camera) {
        queue.removeCallbacks(null);
        queue.post(new Runnable() {
            @Override
            public void run() {
                byte[] data1;
                Camera c;
                if (data != null && camera != null) {
                    data1 = data;
                    c = camera;
                    try {
                        mFrameProcessor.setNextFrame(data1, c);
                    } catch (Exception e) {
                        CrashlyticsUtils.logException(e);
                    }
                }
            }
        });
    }

    public void colorDetectionFramesProcessing(final byte[] data, final Camera camera) {
        queue.removeCallbacks(null);
        queue.post(new Runnable() {
            @Override
            public void run() {
                byte[] data1;
                Camera c;
                if (data != null && camera != null) {
                    data1 = data;
                    c = camera;
                    try {
                        out = new ByteArrayOutputStream();
                        parameters = c.getParameters();
                        size = parameters.getPreviewSize();
                        yuv = new YuvImage(data1, ImageFormat.NV21, size.width,
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
                    } finally {
                        c.addCallbackBuffer(data1);
                        if (bitmap != null) {
                            bitmap.recycle();
                            System.gc();
                        }
                    }
                }
            }
        });
    }

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
            resetPreview();
            takenSuccessfully = true;
            updateLastImage(image.getAbsolutePath());
            notifyFinished();
            return;
        }
    };

    Detector.Processor<Face> detectionProcessor = new Detector.Processor<Face>() {
        @Override
        public void release() {
        }

        @Override
        public void receiveDetections(Detector.Detections<Face> detectionResults) {
            if (graphicsList == null)
                graphicsList = new ArrayList<>();
            ArrayList<FaceDetectionObj> faceDetectionObjArrayList = new ArrayList<>();
            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                android.util.Log.d(TAG, "receiveDetections: " + detectionResults.getDetectedItems().valueAt(i).getId());
                faceDetectionObjArrayList.add(new FaceDetectionObj(detectionResults.getDetectedItems().valueAt(i)));
                boolean isFound = false;
                for (int j = 0; j < graphicsList.size(); j++)
                    if (detectionResults.getDetectedItems().valueAt(i).getId() == ((FaceGraphic) graphicsList.get(j)).getId()) {
                        isFound = true;
                        mFaceGraphic.updateFace(detectionResults.getDetectedItems().valueAt(i));
                        graphicsList.remove(j);
                        break;
                    }
                if (isFound) {
                    ((FaceGraphic) mGraphicOverlay.get(i)).updateFace(detectionResults.getDetectedItems().valueAt(i));
                } else {
                    mFaceGraphic = new FaceGraphic(mGraphicOverlay);
                    mFaceGraphic.setCameraInfo(previewWidth, previewHeight, isBackPreview, faceRotation);
                    mFaceGraphic.setId(detectionResults.getDetectedItems().valueAt(i).getId());
                    mGraphicOverlay.add(mFaceGraphic);
                    mFaceGraphic.updateFace(detectionResults.getDetectedItems().valueAt(i));
                }

            }
            if (detectionResults.getDetectedItems().size() > 0) {
                Message msg = Message.obtain(null, SEND_FACES);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("face_array", faceDetectionObjArrayList);
                bundle.putInt("width", detectionResults.getFrameMetadata().getWidth());
                bundle.putInt("height", detectionResults.getFrameMetadata().getHeight());
                bundle.putInt("rotation", faceRotation);
                msg.setData(bundle);
                try {
                    faceDetectionMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    android.util.Log.d(TAG, "message Exception" + e.getMessage());
                }
            } else if (detectionResults.getDetectedItems().size() == 0) {
                mGraphicOverlay.clear();
                //empty message
                Message msg = Message.obtain(null, SEND_EMPTY, 0, 0);
                try {
                    faceDetectionMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    android.util.Log.d(TAG, "message Exception" + e.getMessage());
                }
            }
            for (int i = 0; i < graphicsList.size(); i++)
                mGraphicOverlay.remove((FaceGraphic) graphicsList.get(i));
            graphicsList = mGraphicOverlay.getFaceGraphicList();
        }
    };

    private final Messenger mMesesenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.replyTo != null && msg.what == GET_RESULT) {
                colorDetectionMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.COLOR_DETECTION_SHIELD.name());
                resetPreview();
                notifyPreviewTypeChanged(isBackPreview, false);
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_OPERATION)
                recevedFrameOperation = ColorDetectionShield.RECEIVED_FRAMES.getEnum(msg.getData().getInt("type"));
            else if (msg.what == ColorDetectionShield.SET_COLOR_DETECTION_TYPE)
                colorType = ColorDetectionShield.COLOR_TYPE.getEnum(msg.getData().getInt("type"));
            else if (msg.what == ColorDetectionShield.SET_COLOR_PATCH_SIZE)
                cellSize = msg.getData().getInt("size");
            else if (msg.what == ColorDetectionShield.UNBIND_COLOR_DETECTOR)
                unBindColorDetector();
            else if (msg.what == CameraShield.UNBIND_CAMERA_CAPTURE)
                unBindCameraCapture();
            else if (msg.what == FaceDetectionShield.UNBIND_FACE_DETECTION)
                unBindFaceDetection();
            else if (msg.what == FaceDetectionShield.BIND_FACE_DETECTION) {
                faceDetectionMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name()))
                    registeredShieldsIDs.add(UIShield.FACE_DETECTION.name());
                resetPreview();
                notifyPreviewTypeChanged(isBackPreview, false);
                Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            } else if (msg.what == IS_FACE_ACTIVE) {
                if (msg.getData().getBoolean("setIsFaceSelected")) {
                    fLayout.addView(mGraphicOverlay);
                    windowManager.updateViewLayout(fLayout, params);
                } else {
                    fLayout.removeView(mGraphicOverlay);
                    windowManager.updateViewLayout(fLayout, params);
                }
            } else if (msg.what == CameraShield.BIND_CAMERA_CAPTURE) {
                cameraMessenger = msg.replyTo;
                if (registeredShieldsIDs != null && !registeredShieldsIDs.contains(UIShield.CAMERA_SHIELD.name()))
                    registeredShieldsIDs.add(UIShield.CAMERA_SHIELD.name());
                resetPreview();
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
            } else if (msg.what == HIDE_PREVIEW)
                hidePreview();
            else if (msg.what == START_DETECTION) {
                detectorVertical = new FaceDetector.Builder(getApplicationContext())
                        .setClassificationType(FaceDetector.ALL_LANDMARKS)
                        .build();
                detectorHorizontal = new FaceDetector.Builder(getApplicationContext())
                        .setClassificationType(FaceDetector.ALL_LANDMARKS)
                        .build();
                startDetection(detectorVertical, detectorHorizontal);
                if (detectorHorizontal.isOperational())
                    detectorHorizontal.setProcessor(detectionProcessor);
                if (detectorVertical.isOperational())
                    detectorVertical.setProcessor(detectionProcessor);

            } else if (msg.what == SET_CAMERA_PREVIEW_TYPE) {
                if (!isCapturing) {
                    if (msg.getData().getBoolean("isBack")) {
                        if (mCamera != null) {
                            queue.removeCallbacks(null);
                            mCamera.setPreviewCallbackWithBuffer(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = Camera.open();
                        } else
                            mCamera = getCameraInstance();
                        try {
                            if (mCamera != null) {
                                parameters = mCamera.getParameters();
                                List<Camera.Size> mSupportedPreviews = mCamera.getParameters().getSupportedPreviewSizes();
                                size = getOptimalPreviewSize(mSupportedPreviews, parameters.getPreviewSize().width, parameters.getPreviewSize().height);
                                parameters.setPreviewSize(size.width, size.height);
                                int[] previewFpsRange = selectPreviewFpsRange(mCamera, 30.0f);
                                if (previewFpsRange == null) {
                                    throw new RuntimeException("Could not find suitable preview frames per second range.");
                                }
                                parameters.setPreviewFpsRange(
                                        previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                                        previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                                mCamera.setParameters(parameters);
                                if (mGraphicOverlay != null)
                                    mGraphicOverlay.clear();
                                setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
                                setPreviewCallbacks(mCamera);
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
                        if (registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name())) {
                            if (detectorVertical != null || detectorHorizontal != null) {
                                mFrameProcessor.setActive(false);
                                detectorHorizontal.release();
                                detectorVertical.release();
                                mFrameProcessor.setActive(true);
                            }
                            detectorVertical = new FaceDetector.Builder(getApplicationContext())
                                    .setClassificationType(FaceDetector.ALL_LANDMARKS)
                                    .build();
                            detectorHorizontal = new FaceDetector.Builder(getApplicationContext())
                                    .setClassificationType(FaceDetector.ALL_LANDMARKS)
                                    .build();
                            startDetection(detectorVertical, detectorHorizontal);
                            if (detectorHorizontal.isOperational())
                                detectorHorizontal.setProcessor(detectionProcessor);
                            if (detectorVertical.isOperational())
                                detectorVertical.setProcessor(detectionProcessor);
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

                        if (registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name())) {
                            if (detectorVertical != null || detectorHorizontal != null) {
                                mFrameProcessor.setActive(false);
                                detectorHorizontal.release();
                                detectorVertical.release();
                                mFrameProcessor.setActive(true);
                            }

                            detectorVertical = new FaceDetector.Builder(getApplicationContext())
                                    .setClassificationType(FaceDetector.ALL_LANDMARKS)
                                    .build();
                            detectorHorizontal = new FaceDetector.Builder(getApplicationContext())
                                    .setClassificationType(FaceDetector.ALL_LANDMARKS)
                                    .build();
                            startDetection(detectorVertical, detectorHorizontal);
                            if (detectorHorizontal.isOperational())
                                detectorHorizontal.setProcessor(detectionProcessor);
                            if (detectorVertical.isOperational())
                                detectorVertical.setProcessor(detectionProcessor);
                        }
                        notifyPreviewTypeChanged(isBackPreview, true);
                    }
                } else
                    notifyPreviewTypeChanged(isBackPreview, true);
            }
        }
    });

    private void startDetection(FaceDetector detectorVertical, FaceDetector detectorHorizontal) {
        mFrameProcessor = new FrameProcessingRunnable(detectorVertical, detectorHorizontal);
        mProcessingThread = new Thread(mFrameProcessor);
        mProcessingThread.setName("face thread");
        mFrameProcessor.setActive(true);
        mProcessingThread.start();
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        registeredShieldsIDs = new ArrayList<>();
    }


    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        Camera.getCameraInfo(cameraId, info);
        WindowManager windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
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

        int angle;
        int displayAngle;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (info.orientation + degrees) % 360;
            displayAngle = (360 - angle) % 360; // compensate for it being mirrored
        } else {  // back-facing
            angle = (info.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }
        dispAngle = displayAngle;
        camera.setDisplayOrientation(displayAngle);
    }

    private void setBestPictureResolution() {
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
        previewWidth = expectedWidth;
        previewHeight = expectedHeight;
        try {
            windowManager.updateViewLayout(fLayout, params);
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
            windowManager.updateViewLayout(fLayout, params);
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
        params.alpha = 1.0f;
        try {
            windowManager.updateViewLayout(fLayout, params);
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
        params.alpha = 1.0f;
        try {
            windowManager.updateViewLayout(fLayout, params);
        } catch (IllegalArgumentException e) {
        }
    }

    private void hidePreview() {
        params.width = 1;
        params.height = 1;
        params.alpha = 1.0f;
        try {
            windowManager.updateViewLayout(fLayout, params);
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
                        setPreviewCallbacks(mCamera);
                        mCamera.startPreview();
                        takePictureWithAutoFocus(mCamera, mCall);

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
                            setPreviewCallbacks(mCamera);
                            mCamera.startPreview();
                            takePictureWithAutoFocus(mCamera, mCall);

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
                    mCamera.setPreviewCallbackWithBuffer(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = Camera.open();
                } else
                    mCamera = getCameraInstance();

                try {
                    if (mCamera != null) {
                        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
                        setPreviewCallbacks(mCamera);
                        mCamera.setPreviewDisplay(sv.getHolder());
                        currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                        notifyPreviewTypeChanged(true, true);
                        parameters = mCamera.getParameters();
                        if (parameters.getSupportedFocusModes().contains(
                                Camera.Parameters.FOCUS_MODE_AUTO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                            FLASH_MODE = "auto";
                        }
                        if (CameraUtils.hasFlash(mCamera)) parameters.setFlashMode(FLASH_MODE);
                        // set biggest picture
                        setBestPictureResolution();
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

    private Camera openFrontFacingCameraGingerbread() {
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallbackWithBuffer(null);
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
        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT, cam);
        parameters = cam.getParameters();
        List<Camera.Size> mSupportedPreviews = cam.getParameters().getSupportedPreviewSizes();
        size = getOptimalPreviewSize(mSupportedPreviews, parameters.getPreviewSize().width, parameters.getPreviewSize().height);
        parameters.setPreviewSize(size.width, size.height);
        int[] previewFpsRange = selectPreviewFpsRange(cam, 15.0f);
        if (previewFpsRange == null) {
            throw new RuntimeException("Could not find suitable preview frames per second range.");
        }
        if (registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name()))
            parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        else if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            cam.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        }
        cam.setParameters(parameters);
        if (mGraphicOverlay != null)
            mGraphicOverlay.clear();
        setPreviewCallbacks(cam);
        return cam;
    }

    private void start() {
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera.cancelAutoFocus();
            mCamera = Camera.open();
        } else
            mCamera = getCameraInstance();
        parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviews = mCamera.getParameters().getSupportedPreviewSizes();
        size = getOptimalPreviewSize(mSupportedPreviews, parameters.getPreviewSize().width, parameters.getPreviewSize().height);
        parameters.setPreviewSize(size.width, size.height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCamera.cancelAutoFocus();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });
        }
        int[] previewFpsRange = selectPreviewFpsRange(mCamera, 15.0f);
        if (previewFpsRange == null) {
            throw new RuntimeException("Could not find suitable preview frames per second range.");
        }
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        mCamera.setParameters(parameters);
        size = parameters.getPreviewSize();
        setPreviewCallbacks(mCamera);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBX_8888);
        params.gravity = Gravity.TOP | Gravity.START;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int expectedHeight = metrics.heightPixels - ((int) (250 * metrics.density + .5f));
        int expectedWidth = ((expectedHeight * (size == null ? metrics.widthPixels : size.height)) / (size == null ? metrics.heightPixels : size.width));
        params.width = 1;// metrics.widthPixels - ((int) (60 * metrics.density + .5f));
        params.height = 1;
        params.x = (int) ((metrics.widthPixels / 2) - expectedWidth / 2);
        params.y = (int) (150 * metrics.density + .5f);
        params.alpha = 1.0f;
        fLayout = new FrameLayout(getApplicationContext());
        FrameLayout.LayoutParams reLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        fLayout.setLayoutParams(reLayoutParams);
        sv = new SurfaceView(getApplicationContext());
        ViewGroup.LayoutParams svParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sv.setLayoutParams(svParams);
        mGraphicOverlay = new GraphicOverlay(getApplicationContext());
        mGraphicOverlay.setLayoutParams(svParams);
        fLayout.addView(sv);
        windowManager.addView(fLayout, params);
        sHolder = sv.getHolder();
        sHolder.addCallback(this);
        if (mGraphicOverlay != null) {
            int min = Math.min(size.width, size.height);
            int max = Math.max(size.width, size.height);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Swap width and height sizes when in portrait, since it will be rotated by
                // 90 degrees
                mGraphicOverlay.setCameraInfo(min, max, Camera.CameraInfo.CAMERA_FACING_BACK);
            } else {
                mGraphicOverlay.setCameraInfo(max, min, Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            mGraphicOverlay.clear();
        }
        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) return;
                mOrientation = (orientation + 45) / 90 * 90;
                //up
                if (orientation >= 0 && orientation < 45)
                    faceRotation = 1;
                    //right & back camera
                else if (orientation >= 45 && orientation < 135)
                    faceRotation = 2;
                    //down
                else if (orientation >= 135 && orientation < 225)
                    faceRotation = 3;
                    //left & back camera
                else if (orientation >= 225 && orientation < 315)
                    faceRotation = 0;
                    //still up
                else if (orientation >= 315 && orientation < 359)
                    faceRotation = 1;
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

    private void setPreviewCallbacks(Camera camera) {
        frame1 = createPreviewBuffer(size);
        frame2 = createPreviewBuffer(size);
        frame3 = createPreviewBuffer(size);
        frame4 = createPreviewBuffer(size);
        if (registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name())) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.addCallbackBuffer(frame1);
            camera.addCallbackBuffer(frame2);
            camera.addCallbackBuffer(frame3);
            camera.addCallbackBuffer(frame4);
        }
        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name())) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.addCallbackBuffer(frame1);
        }
        if (registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()) &&
                registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name())) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.addCallbackBuffer(frame1);
            camera.addCallbackBuffer(frame2);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // sv = new SurfaceView(getApplicationContext());
        if (sv == null)
            start();
        registeredShieldsIDs = new ArrayList<>();
        return START_STICKY;
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
        resetPreview();
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
                if (faceDetectionMessenger != null)
                    faceDetectionMessenger.send(msg);
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
        android.util.Log.d(TAG, "unBindColorDetector: ");
        if (mCamera != null) {
            try {
                if (fLayout != null && registeredShieldsIDs.size() == 1) {
                    if (mCamera != null) {
                        mCamera.setPreviewCallbackWithBuffer(null);
                    }
                    windowManager.removeView(fLayout);
                }
            } catch (Exception e) {

            }
            if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.COLOR_DETECTION_SHIELD.name()))
                registeredShieldsIDs.remove(UIShield.COLOR_DETECTION_SHIELD.name());
            Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            colorDetectionMessenger = null;
        }
    }

    public void unBindFaceDetection() {
        android.util.Log.d(TAG, "unBindFaceDetection: ");
        mFrameProcessor.setActive(false);
        if (mProcessingThread != null) {
            try {
                // Wait for the thread to complete to ensure that we can't have multiple threads
                // executing at the same time (i.e., which would happen if we called start too
                // quickly after stop).
                mProcessingThread.join();
            } catch (InterruptedException e) {
                android.util.Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            mProcessingThread = null;
        }
//         clear the buffer to prevent oom exceptions
        mBytesToByteBuffer.clear();
        if (mCamera != null) {
            try {
                if (fLayout != null && registeredShieldsIDs.size() == 1) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallbackWithBuffer(null);
                    windowManager.removeView(fLayout);
                    mCamera.release();
                    mCamera = null;
                }
            } catch (Exception e) {
            }
            if (registeredShieldsIDs != null && registeredShieldsIDs.contains(UIShield.FACE_DETECTION.name()))
                registeredShieldsIDs.remove(UIShield.FACE_DETECTION.name());
            Log.d("Xcamera", registeredShieldsIDs.size() + "  " + registeredShieldsIDs.toString());
            faceDetectionMessenger = null;
        }
        mFrameProcessor.release();
    }

    public void unBindCameraCapture() {
        if (mCamera != null) {
            try {
                if (fLayout != null && registeredShieldsIDs.size() == 1) {
                    if (mCamera != null) {
                        mCamera.setPreviewCallbackWithBuffer(null);
                    }
                    windowManager.removeView(fLayout);
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
        start();
        return mMesesenger.getBinder();
    }

    private void resetPreview() {
        if (sHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        if (mCamera == null)
            start();
        else {
            try {
                mCamera.setPreviewDisplay(sHolder);
                try {
                    setCameraDisplayOrientation(currentCamera, mCamera);

                } catch (RuntimeException e) {
                }
                setPreviewCallbacks(mCamera);
                mCamera.startPreview();
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
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        try {
            if (sv != null)
                windowManager.removeView(fLayout);
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
        resetPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resetPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            queue.removeCallbacks(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            try {
                if (fLayout != null) {
                    windowManager.removeView(fLayout);
                }
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

    //==============================================================================================
    // Frame processing
    //==============================================================================================

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * Creates one buffer for the camera preview callback.  The size of the buffer is based off of
     * the camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    private byte[] createPreviewBuffer(Camera.Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.height * previewSize.width * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 7.0d) + 1;
        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
        mBytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    /**
     * Selects the most suitable preview frames per second range, given the desired frames per
     * second.
     *
     * @param camera            the camera to select a frames per second range from
     * @param desiredPreviewFps the desired frames per second for the camera preview frames
     * @return the selected preview frames per second range
     */
    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        // The camera API uses integers scaled by a factor of 1000 instead of floating-point frame
        // rates.
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        // The method for selecting the best range is to minimize the sum of the differences between
        // the desired value and the upper and lower bounds of the range.  This may select a range
        // that the desired value is outside of, but this is often preferred.  For example, if the
        // desired frame rate is 29.97, the range (30, 30) is probably more desirable than the
        // range (15, 30).
        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera.  This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     * <p/>
     * While detection is running on a frame, new frames may be received from the camera.  As these
     * frames come in, the most recent frame is held onto as pending.  As soon as detection and its
     * associated processing are done for the previous frame, detection on the mostly recently
     * received frame will immediately start on the same thread.
     */
    private class FrameProcessingRunnable implements Runnable {
        private Detector<?> mDetectorVertical;
        private Detector<?> mDetectorHorizontal;
        private long mStartTimeMillis = SystemClock.elapsedRealtime();

        // This lock guards all of the member variables below.
        private final Object mLock = new Object();
        private boolean mActive = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        private long mPendingTimeMillis;
        private int mPendingFrameId = 0;
        private ByteBuffer mPendingFrameData;

        FrameProcessingRunnable(Detector<?> detectorVertical, Detector<?> detectorHorizontal) {
            mDetectorHorizontal = detectorHorizontal;
            mDetectorVertical = detectorVertical;
        }

        /**
         * Releases the underlying receiver.  This is only safe to do after the associated thread
         * has completed, which is managed in camera source's release method above.
         */
        void release() {
            mDetectorHorizontal.release();
            mDetectorVertical.release();
            mDetectorVertical = null;
            mDetectorHorizontal = null;
        }

        /**
         * Marks the runnable as active/not active.  Signals any blocked threads to continue.
         */
        void setActive(boolean active) {
            synchronized (mLock) {
                mActive = active;
                mLock.notifyAll();
            }
        }

        /**
         * Sets the frame data received from the camera.  This adds the previous unused frame buffer
         * (if present) back to the camera, and keeps a pending reference to the frame data for
         * future use.
         */
        void setNextFrame(byte[] data, Camera camera) {
            synchronized (mLock) {
                if (mPendingFrameData != null) {
                    camera.addCallbackBuffer(mPendingFrameData.array());
                    mPendingFrameData = null;
                }
                if (!mBytesToByteBuffer.containsKey(data)) {
                    android.util.Log.d(TAG,
                            "Skipping frame.  Could not find ByteBuffer associated with the image " +
                                    "data from the camera.");
                    return;
                }

                // Timestamp and frame ID are maintained here, which will give downstream code some
                // idea of the timing of frames received and when frames were dropped along the way.
                mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis;
                mPendingFrameId++;
                mPendingFrameData = mBytesToByteBuffer.get(data);
                // Notify the processor thread if it is waiting on the next frame (see below).
                mLock.notifyAll();
            }
        }

        /**
         * As long as the processing thread is active, this executes detection on frames
         * continuously.  The next pending frame is either immediately available or hasn't been
         * received yet.  Once it is available, we transfer the frame info to local variables and
         * run detection on that frame.  It immediately loops back for the next frame without
         * pausing.
         * <p/>
         * If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context
         * switching or frame acquisition time latency.
         * <p/>
         * If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        @Override
        public void run() {
            Frame outputFrame;
            ByteBuffer data;

            while (true) {
                synchronized (mLock) {
                    while (mActive && (mPendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            mLock.wait();
                        } catch (InterruptedException e) {
                            android.util.Log.d(TAG, "Frame processing loop terminated.", e);
                            return;
                        }
                    }

                    if (!mActive) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return;
                    }

                    //for front face camera rotation
                    if (!isBackPreview && faceRotation == 0)
                        faceRotation = 2;
                    else if (!isBackPreview && faceRotation == 2)
                        faceRotation = 0;
                    else if (!isBackPreview && faceRotation == 1 && dispAngle == 90)
                        faceRotation = 3;
                    else if (!isBackPreview && faceRotation == 3 && dispAngle == 90)
                        faceRotation = 1;
                    outputFrame = new Frame.Builder()
                            .setImageData(mPendingFrameData, size.width,
                                    size.height, ImageFormat.NV21)
                            .setTimestampMillis(mPendingTimeMillis)
                            .setRotation(faceRotation)
                            .setId(mPendingFrameId)
                            .build();
                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear mPendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = mPendingFrameData;
                    mPendingFrameData = null;

                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    if (faceRotation == 1 || faceRotation == 3)
                        mDetectorVertical.receiveFrame(outputFrame);
                    else
                        mDetectorHorizontal.receiveFrame(outputFrame);
                } catch (Throwable t) {
                    android.util.Log.e(TAG, "Exception thrown from receiver.", t);
                } finally {
                    mCamera.addCallbackBuffer(data.array());
                }
            }
        }
    }

    public static class FaceDetectionObj implements Parcelable {
        private int faceId;
        private float xPosition;
        private float yPosition;
        private float height;
        private float width;
        private float leftEye;
        private float rightEye;
        private float isSmile;

        public int getFaceId() {
            return faceId;
        }

        public float getxPosition() {
            return xPosition;
        }

        public float getyPosition() {
            return yPosition;
        }

        public float getHeight() {
            return height;
        }

        public float getWidth() {
            return width;
        }

        public float getLeftEye() {
            return leftEye;
        }

        public float getRightEye() {
            return rightEye;
        }

        public float getIsSmile() {
            return isSmile;
        }

        protected FaceDetectionObj(Parcel in) {
            faceId = in.readInt();
            xPosition = in.readFloat();
            yPosition = in.readFloat();
            height = in.readFloat();
            width = in.readFloat();
            leftEye = in.readFloat();
            rightEye = in.readFloat();
            isSmile = in.readFloat();
        }


        public FaceDetectionObj(Face face) {
            faceId = face.getId();
            xPosition = face.getPosition().x;
            yPosition = face.getPosition().y;
            height = face.getHeight();
            width = face.getWidth();
            leftEye = face.getIsLeftEyeOpenProbability();
            rightEye = face.getIsRightEyeOpenProbability();
            isSmile = face.getIsSmilingProbability();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(faceId);
            dest.writeFloat(xPosition);
            dest.writeFloat(yPosition);
            dest.writeFloat(height);
            dest.writeFloat(width);
            dest.writeFloat(leftEye);
            dest.writeFloat(rightEye);
            dest.writeFloat(isSmile);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<FaceDetectionObj> CREATOR = new Parcelable.Creator<FaceDetectionObj>() {
            @Override
            public FaceDetectionObj createFromParcel(Parcel in) {
                return new FaceDetectionObj(in);
            }

            @Override
            public FaceDetectionObj[] newArray(int size) {
                return new FaceDetectionObj[size];
            }
        };
    }

}