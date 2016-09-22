package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;

import java.util.List;

public class SpeechRecognition implements RecognitionListener {
    @SuppressWarnings("unused")
    private static final String TAG = SpeechRecognition.class.getSimpleName();

    private SpeechRecognizer mSpeechRecognizer;

    public interface RecognitionEventHandler {
        public void onResult(List<String> result);

        public void onError(String error, int errorCode);

        public void onReadyForSpeach(Bundle params);

        public void onBeginingOfSpeech();

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
                mSpeechRecognizer = null;
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
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    public void onBeginningOfSpeech() {
        mResultCallback.onBeginingOfSpeech();
    }

    @Override
    public void onBufferReceived(byte[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEndOfSpeech() {
        mResultCallback.onEndOfSpeech();
    }

    @Override
    public void onError(int error) {

        String reason = "";
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
                    reason = "SpeechRecognizer.ERROR_NO_MATCH";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "SpeechRecognizer.ERROR_RECOGNIZER_BUSY";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "SpeechRecognizer.ERROR_SERVER";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    reason = "SpeechRecognizer.ERROR_SPEECH_TIMEOUT";
                    break;
            }

            mResultCallback.onError(reason, error);
        }

    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // TODO Auto-generated method stub
        Log.d("VR", eventType + "");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // If you feel like it, you can use the partial result.
        // receiveResults(partialResults);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        mResultCallback.onReadyForSpeach(params);
    }

    @Override
    public void onResults(Bundle results) {
        receiveResults(results);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        mResultCallback.onRmsChanged(rmsdB);
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
