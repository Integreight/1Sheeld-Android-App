package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;

import java.text.DecimalFormat;

public class ColorDetectionShield extends
        ControllerParent<ColorDetectionShield> {
    private static final byte SHIELD_ID = (byte) 0x05;
    private static final byte SEND_NORMAL_COLOR = (byte) 0x01;
    private static final byte SEND_FULL_COLORS = (byte) 0x02;
    private static final byte ENABLE_FULL_COLORS = (byte) 0x02;
    private static final byte ENABLE_NORMAL_COLOR = (byte) 0x03;
    private static final byte SET_CALC_MODE = (byte) 0x04;
    private static final byte SET_PATCH_SIZE = (byte) 0x05;
    private static final byte SMALL_PATCH = (byte) 0x01;
    private static final byte MED_PATCH = (byte) 0x02;
    private static final byte AVERAGE_COLOR = (byte) 0x02;
    private static final byte SET_PALLETE = (byte) 0x01;
    private ColorDetectionEventHandler colorEventHandler;
    boolean isCameraBound = false;
    private Messenger mService;
    public static final int UNBIND_COLOR_DETECTOR = 2, SET_COLOR_DETECTION_OPERATION = 10, SET_COLOR_DETECTION_TYPE = 11, SET_COLOR_PATCH_SIZE = 12;
    private RECEIVED_FRAMES recevedFramesOperation = RECEIVED_FRAMES.CENTER;
    private COLOR_TYPE colorType = COLOR_TYPE.COMMON;
    private long lastSentMS = SystemClock.elapsedRealtime();
    private ShieldFrame frame;
    int[] detected;
    ColorPalette currentPallete = ColorPalette._24_BIT_RGB;
    private PATCH_SIZE patchSize = PATCH_SIZE.LARGE;
    public boolean isBackPreview = true;

    @Override

    public ControllerParent<ColorDetectionShield> init(String tag) {
        // TODO Auto-generated method stub
        initMessenger();
        return super.init(tag, true);
    }

    @Override
    public ControllerParent<ColorDetectionShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        addRequiredPremission(Manifest.permission.CAMERA);
        if (!CameraUtils.checkCameraHardware(getApplication().getApplicationContext())) {
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast(R.string.color_detector_camera_is_unavailable_maybe_its_used_by_another_application_toast);
        } else {
            if (checkForPermissions()) {
                if(!activity.canDrawOverApps()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            activity);
                    builder.setMessage(
                            R.string.color_detector_we_need_you_to_enable_the_draw_over_apps_permission_in_order_to_show_the_camera_preview_correctly)
                            .setCancelable(false)
                            .setPositiveButton(R.string.color_detector_validation_dialog_ok_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog,
                                                            final int id) {
                                            activity.requestDrawOverApps();
                                        }
                                    })
                            .setNegativeButton(R.string.color_detector_validation_dialog_later_button, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog,
                                                    final int id) {
                                    dialog.cancel();
                                    activity.showToast(R.string.color_detector_please_enable_the_permission_to_be_able_to_select_this_shield_toast);
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                    if (this.selectionAction != null) {
                        this.selectionAction.onFailure();
                    }
                }
                else {
                    if (selectionAction != null)
                        selectionAction.onSuccess();
                    bindService();
                }
            }else {
                if (selectionAction != null)
                    selectionAction.onFailure();
            }
          }
        return super.invalidate(selectionAction, isToastable);
    }

    private void bindService(){
        getApplication().bindService(new Intent(getActivity(), CameraHeadService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public ColorDetectionShield(Activity activity, String tag) {
        super(activity, tag, true);
    }

    public ColorDetectionShield() {
        super();
    }

    private void notifyPatchSize() {
        if (isCameraBound) {
            Message msg = Message.obtain(null, SET_COLOR_PATCH_SIZE);
            msg.replyTo = mMessenger;
            Bundle data = new Bundle();
            data.putInt("size", patchSize.value);
            msg.setData(data);
            try {
                if (mService != null)
                    mService.send(msg);
            } catch (RemoteException e) {
            }
        } else
            bindService();

    }

    private void notifyColorDetectionOperation() {
        if (isCameraBound) {
            Message msg = Message.obtain(null, SET_COLOR_DETECTION_OPERATION);
            msg.replyTo = mMessenger;
            Bundle data = new Bundle();
            data.putInt("type", recevedFramesOperation.type);
            msg.setData(data);
            try {
                if (mService != null)
                    mService.send(msg);
            } catch (RemoteException e) {
            }
        } else
            bindService();
    }

    private void notifyColorDetectionType() {
        if (isCameraBound) {
            Message msg = Message.obtain(null, SET_COLOR_DETECTION_TYPE);
            msg.replyTo = mMessenger;
            Bundle data = new Bundle();
            data.putInt("type", colorType.type);
            msg.setData(data);
            try {
                if (mService != null)
                    mService.send(msg);
            } catch (RemoteException e) {
            }
        } else
            bindService();

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == SHIELD_ID) {
            switch (frame.getFunctionId()) {
                case ENABLE_FULL_COLORS:
                    if (colorEventHandler != null) {
                        colorEventHandler.enableFullColor();
                    }
                    recevedFramesOperation = RECEIVED_FRAMES.NINE_FRAMES;
                    notifyColorDetectionOperation();
                    break;
                case ENABLE_NORMAL_COLOR:
                    if (colorEventHandler != null) {
                        colorEventHandler.enableNormalColor();
                    }
                    recevedFramesOperation = RECEIVED_FRAMES.CENTER;
                    notifyColorDetectionOperation();
                    break;
                case SET_PALLETE:
                    currentPallete = ColorPalette.get(frame.getArgument(0)[0]);
                    if (colorEventHandler != null) {
                        colorEventHandler.setPallete(currentPallete);
                    }
                    break;
                case SET_CALC_MODE:
                    colorType = frame.getArgument(0)[0] == AVERAGE_COLOR ? COLOR_TYPE.AVERAGE : COLOR_TYPE.COMMON;
                    if (colorEventHandler != null) {
                        colorEventHandler.changeCalculationMode(colorType);
                    }
                    break;
                case SET_PATCH_SIZE:
                    patchSize = frame.getArgument(0)[0] == SMALL_PATCH ? PATCH_SIZE.SMALL : frame.getArgument(0)[0] == MED_PATCH ? PATCH_SIZE.MEDIUM : PATCH_SIZE.LARGE;
                    if (colorEventHandler != null) {
                        colorEventHandler.changePatchSize(patchSize);
                    }
                    notifyPatchSize();
                    break;
            }
        }
    }

    public void setColorEventHandler(ColorDetectionEventHandler colorEventHandler) {
        this.colorEventHandler = colorEventHandler;

    }

    public void showPreview(float x, float y, int width, int height) throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.SHOW_PREVIEW);
        msg.replyTo = mMessenger;
        Bundle b = new Bundle();
        b.putFloat("x", x);
        b.putFloat("y", y);
        b.putInt("w", width);
        b.putInt("h", height);
        msg.setData(b);
        if (mService != null)
            mService.send(msg);
        else
            bindService();

    }

    public void invalidatePreview(float x, float y) throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.INVALIDATE_PREVIEW);
        msg.replyTo = mMessenger;
        Bundle b = new Bundle();
        b.putFloat("x", x);
        b.putFloat("y", y);
        msg.setData(b);
        if (mService != null)
            mService.send(msg);
        else
            bindService();

    }

    public void hidePreview() throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.HIDE_PREVIEW);
        msg.replyTo = mMessenger;
        if (mService != null)
            mService.send(msg);
    }

    public static interface ColorDetectionEventHandler {
        void onColorChanged(int... color);

        void enableFullColor();

        void enableNormalColor();

        void setPallete(ColorPalette pallete);

        void changeCalculationMode(COLOR_TYPE type);

        void changePatchSize(PATCH_SIZE patchSize);

        void onCameraPreviewTypeChanged(final boolean isBack);
    }

    public void setCurrentPallete(ColorPalette currentPallete) {
        this.currentPallete = currentPallete;
    }

    public ColorPalette getCurrentPallete() {
        return currentPallete;
    }

    public RECEIVED_FRAMES getRecevedFramesOperation() {
        return recevedFramesOperation;
    }

    @Override
    public void preConfigChange() {
        super.preConfigChange();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
   }

    @Override
    public void reset() {
        Message msg = Message.obtain(null, UNBIND_COLOR_DETECTOR);
        msg.replyTo = mMessenger;
        try {
            if (mService != null)
                mService.send(msg);
        } catch (RemoteException e) {
        }
        try {
            getApplication().unbindService(mConnection);
        }catch(IllegalArgumentException e){

        }
    }

    boolean fullFrame = true;
    private Messenger mMessenger;

    private void initMessenger() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mMessenger = new Messenger(new Handler() {

                    public void handleMessage(Message msg) {
                        if (msg.what == CameraHeadService.GET_RESULT && msg.getData() != null) {
                            detected = msg.getData().getIntArray("detected");
                            frame = new ShieldFrame(SHIELD_ID, recevedFramesOperation == RECEIVED_FRAMES.NINE_FRAMES ? SEND_FULL_COLORS : SEND_NORMAL_COLOR);
                            int i = 0;
                            for (int det : detected) {
                                int color = getColorInRange(det, currentPallete);
                                detected[i] = color;
                                frame.addArgument(3, color);
                                fullFrame = true;
                                i++;
                            }
                            if (fullFrame && colorEventHandler != null) {
                                colorEventHandler.onColorChanged(detected);
                            }
                            if (fullFrame && SystemClock.elapsedRealtime() - lastSentMS >= 100) {
                                sendShieldFrame(frame);
                                lastSentMS = SystemClock.elapsedRealtime();
                            }
                        } else if (msg.what == CameraHeadService.CRASHED) {
                            bindService();
                        } else if (msg.what == CameraHeadService.SET_CAMERA_PREVIEW_TYPE) {
                            isBackPreview = msg.getData().getBoolean("isBack");
                            if (colorEventHandler != null)
                                colorEventHandler.onCameraPreviewTypeChanged(isBackPreview);
                            isChangingPreview = false;
                        }
                        super.handleMessage(msg);
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    public boolean isBackPreview() {
        return isBackPreview;
    }

    private boolean isChangingPreview = false;

    public boolean setCameraToPreview(boolean isBack) {
        if (!isBack && !CameraUtils.checkFrontCamera(getActivity().getApplicationContext()))
            return false;
        if (isChangingPreview)
            return false;
        isChangingPreview = true;
        Message msg = Message.obtain(null, CameraHeadService.SET_CAMERA_PREVIEW_TYPE);
        msg.replyTo = mMessenger;
        Bundle b = new Bundle();
        b.putBoolean("isBack", isBack);
        msg.setData(b);
        if (mService != null) {
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                return false;
            }
        }
        isBackPreview = isBack;
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            notifyHardwareOfShieldSelection();
            isCameraBound = true;
            mService = new Messenger(binder);
            Message msg = Message.obtain(null, CameraHeadService.GET_RESULT);
            msg.replyTo = mMessenger;
            try {
                mService.send(msg);
                notifyPatchSize();
                notifyColorDetectionType();
                notifyColorDetectionOperation();
            } catch (RemoteException e) {
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            isCameraBound = false;
        }
    };

    public void setRecevedFramesOperation(RECEIVED_FRAMES recevedFramesOperation) {
        this.recevedFramesOperation = recevedFramesOperation;
        notifyColorDetectionOperation();
    }

    public COLOR_TYPE getColorType() {
        return colorType;
    }

    public void setColorType(COLOR_TYPE colorType) {
        this.colorType = colorType;
        notifyColorDetectionType();
    }

    int getColorInRange(int color, ColorPalette palette) {
        int i = 0;
        if (palette.isGrayscale) {
            i = palette.getNumberOfBits();
            int grayscale = (Color.red(color) + Color.green(color) + Color
                    .blue(color)) / 3;
            color = Color.rgb(grayscale, grayscale, grayscale);
        } else
            i = palette.getNumberOfBits() / 3;

        int newR = (int) Math
                .round(((Color.red(color) >>> (8 - i)) * (255 / (Math.pow(2, i) - 1))));
        int newG = (int) Math
                .round(((Color.green(color) >>> (8 - i)) * (255 / (Math.pow(2, i) - 1))));
        int newB = (int) Math
                .round(((Color.blue(color) >>> (8 - i)) * (255 / (Math.pow(2, i) - 1))));
        return Color.rgb(newR, newG, newB);
    }

    public PATCH_SIZE getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(PATCH_SIZE patchSize) {
        this.patchSize = patchSize;
        notifyPatchSize();
    }

    public enum ColorPalette {
        _1_BIT_GRAYSCALE(true, 1, 0), _2_BIT_GRAYSCALE(true, 2, 1), _4_BIT_GRAYSCALE(
                true, 4, 2), _8_BIT_GRAYSCALE(true, 8, 3), _3_BIT_RGB(3, 0), _6_BIT_RGB(
                6, 1), _9_BIT_RGB(9, 2), _12_BIT_RGB(12, 3), _15_BIT_RGB(15, 4), _18_BIT_RGB(
                18, 5), _24_BIT_RGB(24, 6);
        private boolean isGrayscale;
        private int numberOfBits;
        private int index;

        ColorPalette(int numberOfBits, int index) {
            this.isGrayscale = false;
            this.numberOfBits = numberOfBits;
            this.index = index;
        }

        ColorPalette(boolean isGrayscale, int numberOfBits, int index) {
            this.isGrayscale = isGrayscale;
            this.numberOfBits = numberOfBits;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public boolean isGrayscale() {
            return isGrayscale;
        }

        public int getNumberOfBits() {
            return numberOfBits;
        }

        @Override
        public String toString() {
            double colorRange = Math.pow(2, numberOfBits);
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(colorRange)
                    + (isGrayscale ? " Grayscale" : "") + " Colors Palette";
        }

        public static ColorPalette get(byte key) {
            switch (key) {
                case 1:
                    return _1_BIT_GRAYSCALE;
                case 2:
                    return _2_BIT_GRAYSCALE;
                case 3:
                    return _4_BIT_GRAYSCALE;
                case 4:
                    return _8_BIT_GRAYSCALE;
                case 5:
                    return _3_BIT_RGB;
                case 6:
                    return _6_BIT_RGB;
                case 7:
                    return _9_BIT_RGB;
                case 8:
                    return _12_BIT_RGB;
                case 9:
                    return _15_BIT_RGB;
                case 10:
                    return _18_BIT_RGB;
                case 11:
                default:
                    return _24_BIT_RGB;
            }
        }
    }

    public static enum RECEIVED_FRAMES {
        CENTER(0), NINE_FRAMES(1);
        public int type;

        private RECEIVED_FRAMES(int type) {
            this.type = type;
        }

        public static RECEIVED_FRAMES getEnum(int type) {
            return type == 0 ? CENTER : NINE_FRAMES;
        }
    }

    public static enum COLOR_TYPE {
        COMMON(0), AVERAGE(1);
        public int type;

        private COLOR_TYPE(int type) {
            this.type = type;
        }

        public static COLOR_TYPE getEnum(int type) {
            return type == 0 ? COMMON : AVERAGE;
        }
    }

    public static enum PATCH_SIZE {
        SMALL(4), MEDIUM(2), LARGE(1);
        public int value;

        private PATCH_SIZE(int value) {
            this.value = value;
        }

        public static PATCH_SIZE getEnum(byte type) {
            return type == SMALL_PATCH ? SMALL : type == MED_PATCH ? MEDIUM : LARGE;
        }
    }
}