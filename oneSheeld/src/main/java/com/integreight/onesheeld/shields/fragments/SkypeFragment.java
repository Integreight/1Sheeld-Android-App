package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SkypeShield;
import com.integreight.onesheeld.shields.controller.SkypeShield.SkypeEventHandler;

public class SkypeFragment extends ShieldFragmentParent<SkypeFragment> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.skype_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnStart() {
        ((SkypeShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setSkypeEventHandler(skypeEventHandler);

    }

    private SkypeEventHandler skypeEventHandler = new SkypeEventHandler() {

        @Override
        public void onVideoCall(String user) {
            // TODO Auto-generated method stub
            if (canChangeUI())
                Toast.makeText(activity, user + " " +activity.getString(R.string.skype_outgoing_video_call),
                        Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onSkypeClientNotInstalled(String popMessage) {
            // TODO Auto-generated method stub
            if (canChangeUI())
                Toast.makeText(activity, popMessage, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(String error) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onChat(String user) {
            // TODO Auto-generated method stub
            if (canChangeUI())
                Toast.makeText(activity, user + " "+activity.getString(R.string.skype_outgoing_chat),
                        Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCall(String user) {
            // TODO Auto-generated method stub
            if (canChangeUI())
                Toast.makeText(activity, user + " "+activity.getString(R.string.skype_outgoing_call),
                        Toast.LENGTH_SHORT).show();

        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new SkypeShield(activity, getControllerTag()));
            ((SkypeShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setSkypeEventHandler(skypeEventHandler);
        }

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
