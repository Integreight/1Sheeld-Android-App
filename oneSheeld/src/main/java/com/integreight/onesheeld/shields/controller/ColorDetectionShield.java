package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;

public class ColorDetectionShield extends
        ControllerParent<ControllerParent<ColorDetectionShield>> {
    private static final byte SHIELD_ID = (byte) 0x30;
    private static final byte SEND_NORMAL_COLOR = (byte) 0x01;
    private static final byte SEND_FULL_COLORS = (byte) 0x02;
    private static final byte ENABLE_FULL_COLORS = (byte) 0x03;
    private static final byte ENABLE_NORMAL_COLOR = (byte) 0x04;
    private ColorDetectionEventHandler colorEventHandler;
    boolean isCameraBound = false;
    private Messenger mService;
    public static final int UNBIND_COLOR_DETECTOR = 2, SET_COLOR_DETECTION_TYPE = 10;
    private RECEIVED_FRAMES recevedFramesType = RECEIVED_FRAMES.CENTER;
    private long lastSentMS = System.currentTimeMillis();
    private boolean isSendingAframe = false;
    private ShieldFrame frame;
    int[] detected;

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

    private void notifyColorDetectionType() {
        Message msg = Message.obtain(null, CameraHeadService.GET_RESULT);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putInt("type", recevedFramesType.type);
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
                    recevedFramesType = RECEIVED_FRAMES.NINE_FRAMES;
                    notifyColorDetectionType();
                    break;
                case ENABLE_NORMAL_COLOR:
                    recevedFramesType = RECEIVED_FRAMES.CENTER;
                    notifyColorDetectionType();
                    break;
            }
        }
    }

    public void setColorEventHandler(ColorDetectionEventHandler colorEventHandler) {
        this.colorEventHandler = colorEventHandler;

    }

    public static interface ColorDetectionEventHandler {
        void onColorChanged(int... color);
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

    private Messenger mMessenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == CameraHeadService.GET_RESULT && msg.getData() != null) {
                detected = msg.getData().getIntArray("detected");
                if (colorEventHandler != null) {
                    colorEventHandler.onColorChanged(detected);
                }
                if (!isSendingAframe && (System.currentTimeMillis() - lastSentMS > 100)) {
                    isSendingAframe = true;
                    frame = new ShieldFrame(SHIELD_ID, recevedFramesType == RECEIVED_FRAMES.NINE_FRAMES ? SEND_FULL_COLORS : SEND_NORMAL_COLOR);
                    for (int det : detected)
                        frame.addIntegerArgument(2, false, det);
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

    public void setRecevedFramesType(RECEIVED_FRAMES recevedFramesType) {
        this.recevedFramesType = recevedFramesType;
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

}