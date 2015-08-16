package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.ClockShield;
import com.integreight.onesheeld.shields.controller.ClockShield.ClockEventHandler;

public class ClockFragment extends ShieldFragmentParent<ClockFragment> {
    TextView time_tx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.clock_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnStart() {
        ((ClockShield) getApplication().getRunningShields().get(
                getControllerTag())).setClockEventHandler(clockEventHandler);
    }

    @Override
    public void doOnViewCreated(View view, @Nullable Bundle savedInstanceState) {
        time_tx = (TextView) view.findViewById(R.id.time_txt);
    }

    private ClockEventHandler clockEventHandler = new ClockEventHandler() {

        @Override
        public void onTimeChanged(final String time, final boolean isAM) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        time_tx.setBackgroundResource(isAM ? R.drawable.clock_time_rounded_am
                                : R.drawable.clock_time_rounded_pm);
                        time_tx.setText(time);
                    }
                }
            });
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new ClockShield(activity, getControllerTag()));

        }

    }
    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
