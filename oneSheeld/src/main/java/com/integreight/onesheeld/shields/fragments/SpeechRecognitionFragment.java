package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SpeechRecognitionShield;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognition.RecognitionEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.List;

public class SpeechRecognitionFragment extends
        ShieldFragmentParent<SpeechRecognitionFragment> {

    View statusCircle;
    OneSheeldTextView statusHint, recognizedResult;
    TextView rmsIndicator;
    RelativeLayout.LayoutParams params;
    int stepValue = 0;
    int marginValue;
    boolean isDetecting = false;
    String lastResult = "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.voice_recognition_shield_fragment_view,
                container, false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        statusCircle = v.findViewById(R.id.statusCircle);
        statusHint = (OneSheeldTextView) v.findViewById(R.id.statusHint);
        rmsIndicator = (TextView) v.findViewById(R.id.rmsLevelIndicator);
        recognizedResult = (OneSheeldTextView) v
                .findViewById(R.id.recognizedResult);
        recognizedResult.setMovementMethod(new ScrollingMovementMethod());
        params = (LayoutParams) rmsIndicator.getLayoutParams();
        statusCircle.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (stepValue == 0)
                            stepValue = statusCircle.getHeight() / 10;
                    }
                });
    }

    @Override
    public void doOnStart() {
        ((SpeechRecognitionShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setEventHandler(speechRecognitionEventHandler);
        lastResult = ((SpeechRecognitionShield) getApplication().getRunningShields()
                .get(getControllerTag())).getRecognized();
        recognizedResult.setText(lastResult.toLowerCase());
        statusCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isDetecting)
                    ((SpeechRecognitionShield) getApplication().getRunningShields()
                            .get(getControllerTag())).startRecognizer();
                else {
                    ((SpeechRecognitionShield) getApplication().getRunningShields()
                            .get(getControllerTag())).stopListening();
                    setOff();
                }
            }
        });
    }

    @Override
    public void doOnResume() {
        super.doOnResume();
        recognizedResult.setText(lastResult.toLowerCase());
        isDetecting =
                ((SpeechRecognitionShield) getApplication().getRunningShields()
                        .get(getControllerTag())).isWorking();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            lastResult = savedInstanceState.getString("lastResult");
            recognizedResult.setText(lastResult);
        }
    }

    private RecognitionEventHandler speechRecognitionEventHandler = new RecognitionEventHandler() {
        @Override
        public void onResult(final List<String> result) {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setOff();
                        lastResult = result.get(0);
                        recognizedResult.setText(lastResult
                                .toLowerCase());
                    }
                });
        }

        @Override
        public void onReadyForSpeach(Bundle params) {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeUI()) {
                            lastResult = "";
                            recognizedResult.setText(lastResult);
                            setON();
                        }
                    }
                });
        }


        @Override
        public void onError(final String error, int errorCode) {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setOff();
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT)
                                .show();
                        lastResult = "";
                    }
                });
        }

        @Override
        public void onEndOfSpeech() {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setOff();
                    }
                });
        }

        @Override
        public void onBeginningOfSpeech() {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        lastResult = "";
                        recognizedResult.setText(lastResult);
                        setON();
                    }
                });
        }

        @Override
        public void onRmsChanged(final float rmsdB) {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("RMS", rmsdB + "");
                        marginValue = (int) (stepValue * (rmsdB > 10 ? 10 : rmsdB));
                        params.bottomMargin = marginValue < 0 ? 0 : marginValue;
                        rmsIndicator.requestLayout();
                    }
                });
        }
    };

    private void setOff() {
        isDetecting = false;
        rmsIndicator.setVisibility(View.INVISIBLE);
        statusCircle.setBackgroundColor(getResources().getColor(
                R.color.voice_rec_circle_red));
        statusHint.setText(R.string.voice_recognizer_tap_to_speak_button);
    }

    private void setON() {
        isDetecting = true;
        rmsIndicator.setVisibility(View.VISIBLE);
        statusCircle.setBackgroundColor(getResources().getColor(
                R.color.voice_rec_circle_green));
        statusHint.setText(R.string.voice_recognizer_speak);
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new SpeechRecognitionShield(activity, getControllerTag()));
        }

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lastResult", lastResult);
    }
}
