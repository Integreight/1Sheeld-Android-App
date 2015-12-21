package com.integreight.onesheeld.shields.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.shields.controller.NotificationShield.NotificationEventHandler;
import com.integreight.onesheeld.shields.fragments.sub.NotificationShieldSettings;
import com.integreight.onesheeld.utils.customviews.OneSheeldToggleButton;

public class NotificationFragment extends
        ShieldFragmentParent<NotificationFragment> {

    TextView notificationTextTextView;
    OneSheeldToggleButton notificationReceiverToggle;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        hasSettings = true;
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.notification_shield_fragment_layout,
                container, false);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        notificationTextTextView = (TextView) v
                .findViewById(R.id.notification_shield_text_textview);
        notificationReceiverToggle = (OneSheeldToggleButton) v.findViewById(R.id.notification_receiver_toggle);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notificationReceiverToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!((NotificationShield) getApplication().getRunningShields().get(getControllerTag())).startNotificationReceiver())
                            notificationReceiverToggle.setChecked(false);
                    } else {
                        ((NotificationShield) getApplication().getRunningShields().get(getControllerTag())).stopNotificationReceiver();
                    }
                }
            });
        }else{
            notificationReceiverToggle.setVisibility(View.GONE);
        }
        hasSettings = true;
    }

    private NotificationEventHandler notificationEventHandler = new NotificationEventHandler() {

        @Override
        public void onNotificationReceive(final String notificationText) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        notificationTextTextView.setText(notificationText);
                        notificationTextTextView.setTextColor(getResources().getColor(R.color.offWhite));
                    }
                }
            });

        }

        @Override
        public void onNotifiactionArrived(final String notificationText) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        notificationTextTextView.setText(notificationText);
                        notificationTextTextView.setTextColor(getResources().getColor(R.color.green));
                    }
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

    @Override
    public void doOnStart() {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsViewContainer,
                        NotificationShieldSettings.getInstance()).commit();
        super.doOnStart();
    }

    @Override
    public void doOnResume() {
        super.doOnResume();
        ((NotificationShield) getApplication().getRunningShields().get(getControllerTag())).checkDenyList();
    }
}
