package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognition.RecognitionEventHandler;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognitionService;
import com.integreight.onesheeld.utils.Log;

import java.util.List;

public class SpeechRecognitionShield extends
        ControllerParent<SpeechRecognitionShield> {
    private SpeechRecognitionService mSpeechRecognitionService;
    private RecognitionEventHandler eventHandler;
    private static final byte SEND_RESULT = 0x01;
    private static final byte SEND_ERROR = 0x02;
    private boolean isWorking = false;
    private String recognized = null;

    public SpeechRecognitionShield() {
        super();
    }

    public SpeechRecognitionShield(Activity activity, String tag) {
        super(activity, tag, true);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notifyHardwareOfShieldSelection();
            mSpeechRecognitionService = ((SpeechRecognitionService.LocalBinder) service)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSpeechRecognitionService = null;
        }

    };

    @Override
    public ControllerParent<SpeechRecognitionShield> init(String tag) {
        getApplication().bindService(new Intent(activity,
                        SpeechRecognitionService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        Log.sysOut("int AUDIO=" + SpeechRecognizer.ERROR_AUDIO
                + ",NETWORK=" + SpeechRecognizer.ERROR_NETWORK
                + ",NETWORK_TIMEOUT=" + SpeechRecognizer.ERROR_NETWORK_TIMEOUT
                + ",NO_MATCH=" + SpeechRecognizer.ERROR_NO_MATCH
                + ",RECOGNIZER_BUSY=" + SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                + ",SERVER=" + SpeechRecognizer.ERROR_SERVER
                + ",SPEECH_TIMEOUT=" + SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
        return super.init(tag, true);
    }

    @Override
    public ControllerParent<SpeechRecognitionShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        addRequiredPremission(Manifest.permission.RECORD_AUDIO);
        if (!isSpeechRecognitionActivityPresented(activity)) {
            if (isToastable)
                Toast.makeText(activity, R.string.voice_recognizer_please_install_voice_search_from_google_play_store, Toast.LENGTH_SHORT).show();
            selectionAction.onFailure();
        } else if (!checkForPermissions()) {
            selectionAction.onFailure();
        } else
            selectionAction.onSuccess();
        return super.invalidate(selectionAction, isToastable);
    }

    boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            PackageManager pm = callerActivity.getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() != 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void setEventHandler(final RecognitionEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private ShieldFrame sf;
    RecognitionEventHandler controllerHandler = new RecognitionEventHandler() {
        @Override
        public void onResult(List<String> result) {
            if (result != null && result.size() > 0) {
                isWorking = false;
                if (eventHandler != null)
                    eventHandler.onResult(result);
                sf = new ShieldFrame(UIShield.SPEECH_RECOGNIZER_SHIELD.getId(),
                        SEND_RESULT);
                recognized = result.get(0);
                sf.addArgument(recognized.toLowerCase());
                Log.d("Frame", sf.toString());
                sendShieldFrame(sf, true);
            } else {
                onError(activity.getString(R.string.voice_recognizer_no_matching_result), SpeechRecognizer.ERROR_NO_MATCH);
            }

        }

        @Override
        public void onReadyForSpeach(Bundle params) {
            isWorking = true;
            if (eventHandler != null)
                eventHandler.onReadyForSpeach(params);
        }

        @Override
        public void onError(String error, int errorCode) {
            if (eventHandler != null)
                eventHandler.onError(error, errorCode);
            int errorSent = ERROR.SERVER;
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorSent = ERROR.AUDIO;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorSent = ERROR.NETWORK;
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorSent = ERROR.NETWORK_TIMEOUT;
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorSent = ERROR.NO_MATCH;
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorSent = ERROR.RECOGNIZER_BUSY;
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorSent = ERROR.SERVER;
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorSent = ERROR.SPEECH_TIMEOUT;
                    break;

                default:
                    break;
            }
            sf = new ShieldFrame(UIShield.SPEECH_RECOGNIZER_SHIELD.getId(),
                    SEND_ERROR);
            sf.addArgument(1, errorSent);
            Log.d("Frame", sf.toString());
            sendShieldFrame(sf, true);
            isWorking = false;
        }

        @Override
        public void onEndOfSpeech() {
            isWorking = false;
            if (eventHandler != null)
                eventHandler.onEndOfSpeech();
        }

        @Override
        public void onBeginningOfSpeech() {
            isWorking = true;
            if (eventHandler != null)
                eventHandler.onBeginningOfSpeech();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            if (eventHandler != null)
                eventHandler.onRmsChanged(rmsdB);
        }
    };

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.SPEECH_RECOGNIZER_SHIELD.getId()) {
            if (frame.getFunctionId() == 0x01 && !isWorking)
                startRecognizer();

        }
    }

    public void startRecognizer() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isWorking = true;
                if (mSpeechRecognitionService != null)
                    mSpeechRecognitionService
                            .startRecognition(controllerHandler);
            }
        });
    }

    public boolean isWorking() {
        return isWorking;
    }

    public boolean stopListening() {
        if (mSpeechRecognitionService != null)
            mSpeechRecognitionService
                    .stopListening();
        return mSpeechRecognitionService
                .stopListening();
    }

    public String getRecognized() {
        if (recognized != null)
            return recognized;
        else
            return "";
    }

    @Override
    public void reset() {
        if (mServiceConnection != null && getApplication() != null)
            getApplication().unbindService(mServiceConnection);
    }

    private static class ERROR {
        protected static int AUDIO = 3, NETWORK = 2, NETWORK_TIMEOUT = 1,
                NO_MATCH = 7, RECOGNIZER_BUSY = 8, SERVER = 4,
                SPEECH_TIMEOUT = 6;
    }
}
