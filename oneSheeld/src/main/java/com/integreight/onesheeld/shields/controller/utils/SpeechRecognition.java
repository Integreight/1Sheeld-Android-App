package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.integreight.onesheeld.utils.CrashlyticsUtils;

import java.util.List;

public class SpeechRecognition implements RecognitionListener {
    @SuppressWarnings("unused")
    private static final String TAG = SpeechRecognition.class.getSimpleName();
    private long mSpeechRecognizerStartListeningTime = 0;
    private SpeechRecognizer mSpeechRecognizer;
    private boolean isWorking = false;
    private boolean isFinished = true;

    public interface RecognitionEventHandler {
        public void onResult(List<String> result);

        public void onError(String error, int errorCode);

        public void onReadyForSpeach(Bundle params);

        public void onBeginningOfSpeech();

        public void onEndOfSpeech();

        public void onRmsChanged(float rmsdB);
    }

    private RecognitionEventHandler mResultCallback;

    public SpeechRecognition(Context context) {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(this);
    }

    public synchronized void stop() {
        try {
            if (mSpeechRecognizer != null) {
                mSpeechRecognizer.stopListening();
                mSpeechRecognizer.cancel();
                mSpeechRecognizer.destroy();
            }
        } catch (IllegalStateException ignored) {
            CrashlyticsUtils.logException(ignored);
        }
    }

    public void start(RecognitionEventHandler callback) {
        mResultCallback = callback;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.integreight.onesheeld");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        mSpeechRecognizerStartListeningTime = System.currentTimeMillis();
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    public void onBeginningOfSpeech() {
        isWorking = true;
        isFinished = false;
        mResultCallback.onBeginningOfSpeech();
    }

    @Override
    public void onBufferReceived(byte[] arg0) {
    }

    @Override
    public void onEndOfSpeech() {
        mResultCallback.onEndOfSpeech();
    }

    @Override
    public void onError(int error) {
        android.util.Log.d(TAG, "onError: " + error);
        long duration = System.currentTimeMillis() - mSpeechRecognizerStartListeningTime;
        String reason = "";
        if (isFinished)
            return;
        if (mResultCallback != null) {
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "SpeechRecognizer.ERROR_AUDIO";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "SpeechRecognizer.ERROR_CLIENT";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "SpeechRecognizer.ERROR_NETWORK";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "SpeechRecognizer.ERROR_NETWORK_TIMEOUT";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    if (duration < 1000)
                        return;
                    reason = "SpeechRecognizer.ERROR_NO_MATCH";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: {
                    reason = "SpeechRecognizer.ERROR_RECOGNIZER_BUSY";
                    mSpeechRecognizer.stopListening();
                    break;
                }
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "SpeechRecognizer.ERROR_SERVER";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: {
                    reason = "SpeechRecognizer.ERROR_SPEECH_TIMEOUT";
                    break;
                }
            }
            mSpeechRecognizer.cancel();
            mResultCallback.onError(reason, error);
            isWorking = false;
            isFinished = true;
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        isWorking = true;
        isFinished = false;
        mResultCallback.onReadyForSpeach(params);
    }

    @Override
    public void onResults(Bundle results) {
        isWorking = false;
        isFinished = true;
        receiveResults(results);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        mResultCallback.onRmsChanged(rmsdB);
    }

    public boolean stopListening() {
        isWorking = false;
        isFinished = true;
        mSpeechRecognizer.stopListening();
        return true;
    }

    private void receiveResults(Bundle results) {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            List<String> res = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (mResultCallback != null)
                mResultCallback.onResult(res);
        }
    }
}
