package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.VibrationShield;

/**
 * Created by Mostafa on 11/22/15.
 */
public class VibrationFragment extends ShieldFragmentParent<VibrationFragment>{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vibration_shield_fragment_layout,container,false);
    }

    @Override
    public void doOnResume() {
        invalidateController();
    }

    @Override
    public void doOnServiceConnected() {
        invalidateController();
    }

    private void invalidateController() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new VibrationShield(activity, getControllerTag()));
        }
    }
}
