package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.MicShield;
import com.integreight.onesheeld.shields.controller.MicShield.MicEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class MicFragment extends ShieldFragmentParent<MicFragment> {
    RelativeLayout.LayoutParams params;
    TextView soundLevelIndicator, micValue, micState;
    int stepValue;
    private MicEventHandler micEventHandler = new MicEventHandler() {

        @Override
        public void getAmplitude(final Double value) {

            // set data to UI
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        params.bottomMargin = (int) (value * stepValue);
                        if (soundLevelIndicator != null)
                            soundLevelIndicator.requestLayout();
                        micValue.setText(String.valueOf(value).substring(0,
                                4)
                                + " db");
                    }
                }
            });
        }

        @Override
        public void getState(final String state) {
            // set data to UI
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        micState.setText(state);
                    }
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mic_shield_fragment_view, container,
                false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        soundLevelIndicator = (TextView) v
                .findViewById(R.id.soundLevelIndicator);
        micValue = (OneSheeldTextView) v.findViewById(R.id.micValue);
        micState = (OneSheeldTextView) v.findViewById(R.id.micState);
        params = (LayoutParams) soundLevelIndicator.getLayoutParams();
        soundLevelIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        stepValue = soundLevelIndicator.getHeight() / 80;
                    }
                });
    }

    private void invalidateController() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new MicShield(activity, getControllerTag()));
        }

    }

    public void doOnServiceConnected() {
        invalidateController();
    }

    @Override
    public void doOnResume() {
        invalidateController();
        ((MicShield) getApplication().getRunningShields().get(
                getControllerTag())).setMicEventHandler(micEventHandler);
//        ((MicShield) getApplication().getRunningShields().get(
//                getControllerTag())).startMic(false);
        ((MicShield) getApplication().getRunningShields().get(
                getControllerTag())).doOnResume();
    }
}
