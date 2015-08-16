package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;


/**
 * Created by Moustafa Nasr on 6/7/2015.
 */
public class GlcdFragment extends ShieldFragmentParent<GlcdFragment> {
    private GlcdView FragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentView = new GlcdView(activity,GlcdShield.glcdWidth,GlcdShield.glcdHeight, getControllerTag());
        return FragmentView;
    }

    private GlcdShield.GlcdEventHandler glcdEventHandler = new GlcdShield.GlcdEventHandler() {
        @Override
        public void setView(GlcdView glcdView) {
            if (canChangeUI() && uiHandler != null) {
                FragmentView = glcdView;
            }
        }

        @Override
        public GlcdView getView() {
            return FragmentView;
        }

    };

    @Override
    public void doOnStart() {
        ((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(glcdEventHandler);
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new GlcdShield(activity, getControllerTag()));
        }
    }

    @Override
    public void doOnResume() {
        FragmentView.invalidate();
    }
}
