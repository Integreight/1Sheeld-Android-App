package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;


/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdFragment extends ShieldFragmentParent<GlcdFragment> {
    private GlcdView FragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentView = new GlcdView(activity);
        return FragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }


        super.onStart();
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
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new GlcdShield(activity, getControllerTag()));
        }
    }

    @Override
    public void onResume() {
        ((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(glcdEventHandler);
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
