package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.MicSoundMeter;
import com.integreight.onesheeld.utils.Log;

public class MicShield extends ControllerParent<MicShield> {
    Handler handler;
    int PERIOD = 100;
    boolean isHandlerLive = false;
    private MicEventHandler eventHandler;
    private double ampl;
    boolean isResumed = false;
    private ShieldFrame frame;
    public static final byte MIC_VALUE = 0x01;
    boolean initialRequest = true;
    boolean success = true;

    // private int counter = 0;

    private final Runnable processMic = new Runnable() {
        @Override
        public void run() {
            // Do work with the MIC values.
            double amplitude = MicSoundMeter.getInstance().getAmplitudeEMA();
            if (!Double.isInfinite(amplitude) && amplitude != 0) {
                initialRequest = false;
                ampl = amplitude;
                Log.d("MIC", "Amp = " + ampl);
                frame = new ShieldFrame(UIShield.MIC_SHIELD.getId(), MIC_VALUE);
                frame.addByteArgument((byte) Math.round(ampl));
                sendShieldFrame(frame);
                if (isResumed)
                    if (eventHandler != null)
                        eventHandler.getAmplitude(ampl);
            }
            // The Runnable is posted to run again here:
            if (handler != null)
                handler.postDelayed(this, PERIOD);
        }
    };

    public MicShield() {
    }

    public void doOnResume() {
        isResumed = true;
    }

    public MicShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<MicShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<MicShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        if (activity.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE))
            startMic(isToastable);
        else
            this.selectionAction.onFailure();
        return super.invalidate(selectionAction, isToastable);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        stopMic();
    }

    public void startMic(boolean isToastable) {
        final boolean isRecording = MicSoundMeter.getInstance().start();
        if (!isRecording)
            success = false;
        handler = new Handler();
        if (selectionAction != null) {
            if (success)
                selectionAction.onSuccess();
        }
        if (processMic != null)
            handler.post(processMic);

    }

    public void stopMic() {
        if (handler != null) {
            if (processMic != null)
                handler.removeCallbacks(processMic);
        }
        MicSoundMeter.getInstance().stop();
    }

    public static interface MicEventHandler {
        void getAmplitude(Double value);
    }

    public void setMicEventHandler(MicEventHandler micEventHandler) {
        this.eventHandler = micEventHandler;

    }

}
