package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;

import java.text.DecimalFormat;

public class ColorDetectionShield extends
        ControllerParent<ControllerParent<ColorDetectionShield>> {
    private static final byte SHIELD_ID = (byte) 0x05;
    private static final byte SEND_NORMAL_COLOR = (byte) 0x01;
    private static final byte SEND_FULL_COLORS = (byte) 0x02;
    private static final byte ENABLE_FULL_COLORS = (byte) 0x02;
    private static final byte ENABLE_NORMAL_COLOR = (byte) 0x03;
    private static final byte SET_CALC_MODE = (byte) 0x04;
    private static final byte SET_PATCH_SIZE = (byte) 0x05;
    private static final byte SMALL_PATCH = (byte) 0x01;
    private static final byte MED_PATCH = (byte) 0x02;
    private static final byte LARGE_PATCH = (byte) 0x03;
    private static final byte COMMON_COLOR = (byte) 0x01;
    private static final byte AVERAGE_COLOR = (byte) 0x02;
    private static final byte SET_PALLETE = (byte) 0x01;
    private ColorDetectionEventHandler colorEventHandler;
    boolean isCameraBound = false;
    private Messenger mService;
    public static final int UNBIND_COLOR_DETECTOR = 2, SET_COLOR_DETECTION_OPERATION = 10, SET_COLOR_DETECTION_TYPE = 11, SET_COLOR_PATCH_SIZE = 12;
    private RECEIVED_FRAMES recevedFramesOperation = RECEIVED_FRAMES.NINE_FRAMES;
    private COLOR_TYPE colorType = COLOR_TYPE.COMMON;
    private long lastSentMS = System.currentTimeMillis();
    private boolean isSendingAframe = false;
    private ShieldFrame frame;
    int[] detected;
    ColorPalette currentPallete = ColorPalette._24_BIT_RGB;
    private float[][] hsv = null;
    private PATCH_SIZE patchSize = PATCH_SIZE.LARGE;

    @Override

    public ControllerParent<ControllerParent<ColorDetectionShield>> init(String tag) {
        // TODO Auto-generated method stub
        getActivity().bindService(new Intent(getActivity(), CameraHeadService.class), mConnection, Context.BIND_AUTO_CREATE);
        return super.init(tag);
    }

    public ColorDetectionShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public ColorDetectionShield() {
        super();
    }

    private void notifyPatchSize() {
        Message msg = Message.obtain(null, SET_COLOR_PATCH_SIZE);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putInt("size", patchSize.value);
        msg.setData(data);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
        }
    }

    private void notifyColorDetectionOperation() {
        Message msg = Message.obtain(null, SET_COLOR_DETECTION_OPERATION);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putInt("type", recevedFramesOperation.type);
        msg.setData(data);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
        }
    }

    private void notifyColorDetectionType() {
        Message msg = Message.obtain(null, SET_COLOR_DETECTION_TYPE);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putInt("type", colorType.type);
        msg.setData(data);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
        }
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
                        colorEventHandler.changePathSize(patchSize);
                    }
                    notifyPatchSize();
                    break;
            }
        }
    }

    public void setColorEventHandler(ColorDetectionEventHandler colorEventHandler) {
        this.colorEventHandler = colorEventHandler;

    }

    public static interface ColorDetectionEventHandler {
        void onColorChanged(int... color);

        void enableFullColor();

        void enableNormalColor();

        void setPallete(ColorPalette pallete);

        void changeCalculationMode(COLOR_TYPE type);

        void changePathSize(PATCH_SIZE patchSize);
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
    public void reset() {
        Message msg = Message.obtain(null, UNBIND_COLOR_DETECTOR);
        msg.replyTo = mMessenger;
        try {
            if (mService != null)
                mService.send(msg);
        } catch (RemoteException e) {
        }
        getActivity().unbindService(mConnection);
    }

    boolean fullFrame = true;
    private Messenger mMessenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == CameraHeadService.GET_RESULT && msg.getData() != null) {
                detected = msg.getData().getIntArray("detected");
                if (!isSendingAframe && (System.currentTimeMillis() - lastSentMS >= 100)) {
                    isSendingAframe = true;
                    hsv = new float[recevedFramesOperation == RECEIVED_FRAMES.NINE_FRAMES ? 9 : 1][3];
                    frame = new ShieldFrame(SHIELD_ID, recevedFramesOperation == RECEIVED_FRAMES.NINE_FRAMES ? SEND_FULL_COLORS : SEND_NORMAL_COLOR);
                    int i = 0;
                    for (int det : detected) {
                        int color = getColorInRange(det, currentPallete);
                        detected[i] = color;
                        frame.addIntegerArgument(3, color);
                        fullFrame = true;
                        if (i < hsv.length) {
                            Color.colorToHSV(color, hsv[i]);
                            int h = Math.round(hsv[i][0]);
                            int s = Math.round(hsv[i][1] * 100);
                            int v = Math.round(hsv[i][2] * 100);
                            int hsvColor = h << 16 | s << 8 | v;
                            frame.addIntegerArgument(4, hsvColor);
                        } else {
                            fullFrame = false;
                            break;
                        }
                        i++;
                    }
                    if (fullFrame) {
                        if (colorEventHandler != null) {
                            colorEventHandler.onColorChanged(detected);
                        }
                        sendShieldFrame(frame);
                    }
                    isSendingAframe = false;
                    lastSentMS = System.currentTimeMillis();
                }
            } else {
                super.handleMessage(msg);
            }
        }
    });

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            mService = new Messenger(binder);
            Message msg = Message.obtain(null, CameraHeadService.GET_RESULT);
            msg.replyTo = mMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
            }
            isCameraBound = true;
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

    public enum ColorPalette {
        _1_BIT_GRAYSCALE(true, 1), _2_BIT_GRAYSCALE(true, 2), _4_BIT_GRAYSCALE(
                true, 4), _8_BIT_GRAYSCALE(true, 8), _3_BIT_RGB(3), _6_BIT_RGB(
                6), _9_BIT_RGB(9), _12_BIT_RGB(12), _15_BIT_RGB(15), _18_BIT_RGB(
                18), _24_BIT_RGB(24);
        private boolean isGrayscale;
        private int numberOfBits;

        ColorPalette(int numberOfBits) {
            this.isGrayscale = false;
            this.numberOfBits = numberOfBits;
        }

        ColorPalette(boolean isGrayscale, int numberOfBits) {
            this.isGrayscale = isGrayscale;
            this.numberOfBits = numberOfBits;
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