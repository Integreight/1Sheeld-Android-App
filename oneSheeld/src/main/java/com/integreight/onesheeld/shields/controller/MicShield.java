package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.integreight.onesheeld.BuildConfig;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.MicSoundMeter;
import com.integreight.onesheeld.utils.Log;

import java.io.File;
import java.util.Date;

public class MicShield extends ControllerParent<MicShield> {
    public static final byte MIC_VALUE = 0x01;
    private static final byte MIC_START_RECORD = 0x01;
    private static final byte MIC_STOP_RECORD = 0x02;
    Handler handler;
    int PERIOD = 100;
    boolean isHandlerLive = false;
    boolean isResumed = false;
    boolean initialRequest = true;
    boolean success = true;
    private MicEventHandler eventHandler;
    private double ampl;
    private ShieldFrame frame;
    private final Runnable processMic = new Runnable() {
        @Override
        public void run() {
            // Do work with the MIC values.
            double amplitude = MicSoundMeter.getInstance().getAmplitudeEMA();
            if (!Double.isInfinite(amplitude) && amplitude != 0) {
                initialRequest = false;
                ampl = amplitude;
//                Log.d("MIC", "Amp = " + ampl);
                frame = new ShieldFrame(UIShield.MIC_SHIELD.getId(), MIC_VALUE);
                frame.addArgument((byte) Math.round(ampl));
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
    private boolean isRecording = false;

    // private int counter = 0;
    private String fileName = "";

    public MicShield() {
    }

    public MicShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public void doOnResume() {
        isResumed = true;
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
        addRequiredPremission(Manifest.permission.RECORD_AUDIO);
        addRequiredPremission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(Build.VERSION.SDK_INT >=16)
        addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (activity.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE) && checkForPermissions())
            startMic(isToastable);
        else
            this.selectionAction.onFailure();
        return super.invalidate(selectionAction, isToastable);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.MIC_SHIELD.getId()) {
            switch (frame.getFunctionId()) {
                case MIC_START_RECORD:
                    if (!isRecording) {
                        handler.removeCallbacks(processMic);
                        MicSoundMeter.getInstance().stop();
                        if (frame.getArguments().isEmpty()) {
                            fileName = "Mic_" + String.valueOf(new Date().getTime());
                        } else {
                            fileName = frame.getArgumentAsString(0) +"_" + String.valueOf(new Date().getTime());
                        }
                        MicSoundMeter.getInstance().start(true, fileName);
                        if (eventHandler != null)
                            eventHandler.getState(activity.getString(R.string.mic_recording)+"...");
                        handler.post(processMic);
                        isRecording = true;
                    }
                    break;
                case MIC_STOP_RECORD:
                    if (isRecording) {
                        handler.removeCallbacks(processMic);
                        MicSoundMeter.getInstance().stop();
                        MicSoundMeter.getInstance().start(false);
                        if (eventHandler != null)
                            eventHandler.getState("");
                        if (!fileName.equals(""))
                            showNotification(activity.getString(R.string.mic_sound_recorded_successfully_to)+" " + fileName + ".mp3");
                        fileName = "";
                        handler.post(processMic);
                        isRecording = false;
                    }
                    break;
            }
        }
    }

    @Override
    public void reset() {
        stopMic();
        if (!fileName.equals(""))
            showNotification(activity.getString(R.string.mic_sound_recorded_successfully_to)+" " + fileName + ".mp3");
        fileName = "";
    }

    public void startMic(boolean isToastable) {
        final boolean isRecording = MicSoundMeter.getInstance().start(false);
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

    protected void showNotification(String notificationText) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                activity);
        build.setSmallIcon(OneSheeldApplication.getNotificationIcon());
        build.setContentTitle(activity.getString(R.string.mic_shield_name)+" Shield");
        build.setContentText(notificationText);
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        build.setAutoCancel(true);
        Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) activity
                .getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        Log.d("Mic",fileName+".mp3");
        if(Build.VERSION.SDK_INT>=24) {
            Uri fileURI = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    new File(Environment.getExternalStorageDirectory() + "/OneSheeld/Mic/" + fileName + ".mp3"));
            notificationIntent.setDataAndType(fileURI, "audio/*");
        }else{
            notificationIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/OneSheeld/Mic/"+ fileName + ".mp3")), "audio/*");
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) new Date().getTime(), notification);
    }

    public void setMicEventHandler(MicEventHandler micEventHandler) {
        this.eventHandler = micEventHandler;

    }

    public static interface MicEventHandler {
        void getAmplitude(Double value);

        void getState(String state);
    }

}
