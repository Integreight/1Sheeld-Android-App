package com.integreight.onesheeld.shields.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield.TTsEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TextToSpeechFragment extends
        ShieldFragmentParent<TextToSpeechFragment> {

    ImageView speakerLevel;
    Button femaleBtn, maleBtn;
    OneSheeldTextView ttsText;
    AnimationDrawable animation;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tts_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        speakerLevel = (ImageView) v
                .findViewById(R.id.speaker_shield_imageview);
        femaleBtn = (Button) v.findViewById(R.id.increaseBtn);
        maleBtn = (Button) v.findViewById(R.id.decreaseBtn);
        ttsText = (OneSheeldTextView) v.findViewById(R.id.ttsText);
        ttsText.setMovementMethod(new ScrollingMovementMethod());
        animation = (AnimationDrawable) speakerLevel.getBackground();
    }

    private Runnable runAnimation = new Runnable() {

        @Override
        public void run() {
            animation.start();
        }
    };

    @Override
    public void doOnStart() {
        uiHandler = new Handler();
        ((TextToSpeechShield) getApplication().getRunningShields().get(
                getControllerTag())).setEventHandler(ttsEventHandler);
    }

    @Override
    public void doOnStop() {
        if (speakerLevel != null && animation != null) {
            speakerLevel.removeCallbacks(runAnimation);
            animation.stop();
        }
    }

    private TTsEventHandler ttsEventHandler = new TTsEventHandler() {

        @Override
        public void onSpeek(final String txt) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        speakerLevel
                                .setBackgroundResource(R.anim.tts_animation);
                        ttsText.setText(txt);
                        animation = (AnimationDrawable) speakerLevel
                                .getBackground();
                        speakerLevel.post(runAnimation);
                    }
                }
            });
        }

        @Override
        public void onError(String error, int errorCode) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        speakerLevel.removeCallbacks(runAnimation);
                        animation.stop();
                    }
                }
            });

        }

        @Override
        public void onStop() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        speakerLevel.removeCallbacks(runAnimation);
                        animation.stop();
                        speakerLevel
                                .setBackgroundResource(R.drawable.tts_shield_0_volume);
                    }
                }
            });
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new TextToSpeechShield(activity, getControllerTag()));
        }
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
