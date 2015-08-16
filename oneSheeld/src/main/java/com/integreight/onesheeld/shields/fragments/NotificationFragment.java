package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.shields.controller.NotificationShield.NotificationEventHandler;

public class NotificationFragment extends
        ShieldFragmentParent<NotificationFragment> {

    TextView notificationTextTextView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.notification_shield_fragment_layout,
                container, false);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        notificationTextTextView = (TextView) v
                .findViewById(R.id.notification_shield_text_textview);
    }

    private NotificationEventHandler notificationEventHandler = new NotificationEventHandler() {

        @Override
        public void onNotificationReceive(final String notificationText) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        notificationTextTextView.setText(notificationText);
                }
            });

        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new NotificationShield(activity, getControllerTag()));
        ((NotificationShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setNotificationEventHandler(notificationEventHandler);
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
