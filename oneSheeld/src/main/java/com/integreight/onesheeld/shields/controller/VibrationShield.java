package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
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
    private VibrationShieldListener vibrationShieldListener;
    private Runnable onStart;
    private Runnable onPause;
    private Runnable onStop;
    private boolean isVibrating;
    private boolean isPaused;

    public interface VibrationShieldListener{
        void onStart();
        void onPause();
        void onStop();
    }

    public VibrationShield(){
        super();
    }

    public VibrationShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public VibrationShield(Activity activity, String tag, boolean manageShieldSelectionFrameManually) {
        super(activity, tag, manageShieldSelectionFrameManually);
    }

    public void setVibrationShieldListener(VibrationShieldListener listener){
        this.vibrationShieldListener = listener;
    }

    public boolean isVibrating(){
        synchronized (this) {
            return isVibrating;
        }
    }

    public boolean isPaused(){
        synchronized (this) {
            return isPaused;
        }
    }

    @Override
    public ControllerParent<VibrationShield> init(String tag) {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        if(Build.VERSION.SDK_INT >= 21)
            scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        onStart = new Runnable() {
            @Override
            public void run() {
                if (vibrationShieldListener != null) vibrationShieldListener.onStart();
                synchronized (VibrationShield.this) {
                    isVibrating = true;
                    isPaused = false;
                }
            }
        };
        onPause = new Runnable() {
            @Override
            public void run() {
                if (vibrationShieldListener != null) vibrationShieldListener.onPause();
                synchronized (VibrationShield.this) {
                    isVibrating = false;
                    isPaused = true;
                }
            }
        };
        onStop = new Runnable() {
            @Override
            public void run() {
                if (vibrationShieldListener != null) vibrationShieldListener.onStop();
                synchronized (VibrationShield.this) {
                    isVibrating = false;
                    isPaused = false;
                }
            }
        };
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
                activity.showToast(activity.getString(R.string.vibration_your_device_doesnt_support_this_hardware_toast));
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

                    final long[] stoppingPattern = new long[receivedPattern.length/2];
                    stoppingPattern[0] = pattern[0];
                    for (int i = 1 ; i < stoppingPattern.length ; i++){
                        stoppingPattern[i] = stoppingPattern[i-1] + pattern[i];
                    }

                    if (period == NO_REPEAT) {
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                if (vibrationShieldListener != null) vibrationShieldListener
                                        .onPause();
                                synchronized (VibrationShield.this) {
                                    isVibrating = false;
                                    isPaused = true;
                                }
                                vibrator.vibrate(pattern, -1);
                            }
                        }, 0, TimeUnit.MILLISECONDS));

                        for(int i = 0 ; i < stoppingPattern.length-1 ; i++){
                            if(i%2 == 0){
                                futureTasks.add(scheduledThreadPoolExecutor.schedule(onStart,
                                        stoppingPattern[i], TimeUnit.MILLISECONDS));
                            }
                            else{
                                futureTasks.add(scheduledThreadPoolExecutor.schedule(onPause,
                                        stoppingPattern[i], TimeUnit.MILLISECONDS));
                            }
                        }
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(onStop,
                                stoppingPattern[stoppingPattern.length-1], TimeUnit.MILLISECONDS));
                    }
                    else {

                        for (long i:pattern)
                            period+= i;

                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                                new Runnable() {
                            @Override
                            public void run() {
                                if (vibrationShieldListener != null) vibrationShieldListener
                                        .onPause();
                                synchronized (VibrationShield.this) {
                                    isVibrating = false;
                                    isPaused = true;
                                }
                                vibrator.vibrate(pattern, -1);
                            }
                        }, 0, period, TimeUnit.MILLISECONDS));

                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                                new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < stoppingPattern.length - 1; i++) {
                                    if (i % 2 == 0) {
                                        futureTasks.add(scheduledThreadPoolExecutor.schedule(onStart,
                                                stoppingPattern[i], TimeUnit.MILLISECONDS));
                                    } else {
                                        futureTasks.add(scheduledThreadPoolExecutor.schedule(onPause,
                                                stoppingPattern[i], TimeUnit.MILLISECONDS));
                                    }
                                }
                                futureTasks.add(scheduledThreadPoolExecutor.schedule(onPause,
                                        stoppingPattern[stoppingPattern.length - 1],
                                        TimeUnit.MILLISECONDS));
                            }
                        },0,period,TimeUnit.MILLISECONDS));
                    }
                    break;
                case 0x02:
                    final int duration = frame.getArgumentAsInteger(0);
                    period = frame.getArgumentAsInteger(1);
                    if (period == NO_REPEAT) {
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                if (vibrationShieldListener != null) vibrationShieldListener
                                        .onStart();
                                synchronized (VibrationShield.this) {
                                    isVibrating = true;
                                    isPaused = false;
                                }
                                vibrator.vibrate(duration);
                            }
                        }, 0, TimeUnit.MILLISECONDS));
                        futureTasks.add(scheduledThreadPoolExecutor.schedule(onStop,
                                duration,TimeUnit.MILLISECONDS));
                    }
                    else {
                        period += duration;
                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                                new Runnable() {
                            @Override
                            public void run() {
                                if (vibrationShieldListener != null) vibrationShieldListener
                                        .onStart();
                                synchronized (VibrationShield.this) {
                                    isVibrating = true;
                                    isPaused = false;
                                }
                                vibrator.vibrate(duration);
                            }
                        }, 0, period, TimeUnit.MILLISECONDS));
                        futureTasks.add(scheduledThreadPoolExecutor.scheduleWithFixedDelay(onPause,
                                duration,period,TimeUnit.MILLISECONDS));
                    }
                    break;
                case 0x03:
                    stop();
                    break;
            }
        }
    }

    public void stop(){
        if (vibrationShieldListener != null) vibrationShieldListener.onStop();
        synchronized (VibrationShield.this) {
            isVibrating = false;
            isPaused = false;
        }
        ((Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        for (ScheduledFuture task:futureTasks)
            task.cancel(true);
        futureTasks.clear();
    }

    @Override
    public void reset() {
        ((Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        if (!scheduledThreadPoolExecutor.isShutdown())
            scheduledThreadPoolExecutor.shutdown();
        futureTasks.clear();
    }
}
