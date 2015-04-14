package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;

public class ColorDetectionFragment extends ShieldFragmentParent<ColorDetectionFragment> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.camera_shield_fragment_layout, container,
                false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        super.onStart();

    }


    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
