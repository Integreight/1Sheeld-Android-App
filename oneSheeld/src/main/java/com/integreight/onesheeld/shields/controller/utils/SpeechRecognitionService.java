package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.integreight.onesheeld.utils.Log;

public class SpeechRecognitionService extends Service {
    private static final String TAG = SpeechRecognitionService.class
            .getSimpleName();

    private SpeechRecognition mSpeechRecognition;

    public class LocalBinder extends Binder {
        public SpeechRecognitionService getService() {
            return SpeechRecognitionService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSpeechRecognition = new SpeechRecognition(this);
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    public boolean stopListening(){
        mSpeechRecognition.stopListening();
        return true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognition != null)
            mSpeechRecognition.stop();
        Log.d(TAG, "onDestroy");
    }

    public void startRecognition(
            SpeechRecognition.RecognitionEventHandler callback) {
        mSpeechRecognition.start(callback);
    }
}
