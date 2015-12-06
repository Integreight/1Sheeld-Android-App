package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;

import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Mostafa Mahmoud
 * Email: mostafa_mahmoud@protonmail.com
 * Created on: 11/26/15
 */
public class VibrationShield extends ControllerParent<VibrationShield> {
    private static final int NO_REPEAT = 65535 ; //65535 is the 2'complement of -1
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private Vector<ScheduledFuture<?>> futureTasks;

    public VibrationShield(){
        super();
    }

    public VibrationShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public VibrationShield(Activity activity, String tag, boolean manageShieldSelectionFrameManually) {
        super(activity, tag, manageShieldSelectionFrameManually);
    }

    @Override
    public ControllerParent<VibrationShield> init(String tag) {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        if(Build.VERSION.SDK_INT >= 21)
            scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        futureTasks = new Vector<>();
        return super.init(tag);
    }

    @Override
    public ControllerParent<VibrationShield> invalidate(SelectionAction selectionAction, boolean isToastable) {
        this.selectionAction = selectionAction;
        boolean hasVibrator;
        if(Build.VERSION.SDK_INT >=11)
            hasVibrator = ((Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE))
                    .hasVibrator();
        else
            hasVibrator = getApplication().getSystemService(Context.VIBRATOR_SERVICE) != null;

        if(hasVibrator) {
            Log.d("Vibrator Availability","Available");
            if (selectionAction !=null)
                selectionAction.onSuccess();
        }
        else {
            Log.d("Unavailable Hardware", "Vibration");
            if (selectionAction !=null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast("Device doesn't support this hardware!");
        }
        return super.invalidate(selectionAction, isToastable);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if(frame.getShieldId() == UIShield.VIBRATION_SHIELD.getId()){
            final Vibrator vibrator = (Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE);
            int period;
            stop();
            switch (frame.getFunctionId()){
                case 0x01:
                    final byte[] receivedPattern = frame.getArgument(0);
                    period = frame.getArgumentAsInteger(1);

                    final long[] pattern = new long[receivedPattern.length/2];
                    for(int i = 0 ; i < pattern.length ; i++){
                        pattern[i] = ((receivedPattern[2*i+1] & 0xFFL) <<8 ) | (receivedPattern[i*2] & 0xFFL);
                    }

                    if (period == NO_REPEAT)
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                vibrator.vibrate(pattern,-1);
                            }
                        },0,TimeUnit.MILLISECONDS));
                    else {
                        for (long i:pattern)
                            period+= i;
                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                vibrator.vibrate(pattern,-1);
                            }
                        }, 0, period, TimeUnit.MILLISECONDS));
                    }
                    break;
                case 0x02:
                    final int duration = frame.getArgumentAsInteger(0);
                    period = frame.getArgumentAsInteger(1);
                    if (period == NO_REPEAT)
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                vibrator.vibrate(duration);
                            }
                        },0,TimeUnit.MILLISECONDS));
                    else {
                        period += duration;
                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                vibrator.vibrate(duration);
                            }
                        }, 0, period, TimeUnit.MILLISECONDS));
                    }
                    break;
                case 0x03:
                    stop();
                    break;
            }

        }
    }

    public void stop(){
        ((Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        for (ScheduledFuture task:futureTasks)
            task.cancel(true);
        futureTasks.clear();
    }

    @Override
    public void reset() {
        Log.sysOut("Reset");
        ((Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        if (!scheduledThreadPoolExecutor.isShutdown())
            scheduledThreadPoolExecutor.shutdown();
        futureTasks.clear();
    }
}
