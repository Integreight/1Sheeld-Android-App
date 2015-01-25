package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.PatternShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.utils.customviews.LockPatternViewEx;

import java.util.List;

public class PatternFragment extends ShieldFragmentParent<PatternFragment> {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.pattern_shield_fragment_layout, container,
                false);
        return v;

    }

    @Override
    public void onStart() {

        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        if (getView() != null && getView().findViewById(R.id.lockPattern) != null)
            ((LockPatternViewEx) getView().findViewById(R.id.lockPattern)).setOnPatternListener(new LockPatternViewEx.OnPatternListener() {
                @Override
                public void onPatternStart() {

                }

                @Override
                public void onPatternCleared() {

                }

                @Override
                public void onPatternCellAdded(List<LockPatternViewEx.Cell> pattern) {

                }

                @Override
                public void onPatternDetected(List<LockPatternViewEx.Cell> pattern) {
                    ((PatternShield) getApplication().getRunningShields().get(getControllerTag())).onPatternDetected(pattern);
                }
            });
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new SpeakerShield(activity, getControllerTag()));
        }

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
