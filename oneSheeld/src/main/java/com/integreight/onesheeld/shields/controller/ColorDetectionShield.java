package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    private ColorDetectionEventHandler colorEventHandler;
    boolean isCameraBound = false;
    private Messenger mService;
    public static final int UNBIND_COLOR_DETECTOR = 2;

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

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == SHIELD_ID) {

        }
    }

    public void setClockEventHandler(ColorDetectionEventHandler colorEventHandler) {
        this.colorEventHandler = colorEventHandler;

    }

    public static interface ColorDetectionEventHandler {
        void onColorChanged(int common, int dominant);
    }

    @Override
    public void reset() {
        Message msg = Message.obtain(null, UNBIND_COLOR_DETECTOR);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
        }
        getActivity().unbindService(mConnection);
    }

    private Messenger mMessenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == CameraHeadService.GET_RESULT && msg.getData() != null) {
                if (colorEventHandler != null) {
                    colorEventHandler.onColorChanged(msg.getData().getInt("common"), msg.getData().getInt("dominant"));
                }
            } else {
                super.handleMessage(msg);
            }
        }

        ;
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

}