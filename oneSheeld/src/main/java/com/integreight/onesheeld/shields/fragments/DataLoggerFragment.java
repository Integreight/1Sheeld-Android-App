package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.DataLoggerShield;
import com.integreight.onesheeld.shields.controller.DataLoggerShield.DataLoggerListener;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.ArrayList;
import java.util.Map;

public class DataLoggerFragment extends
        ShieldFragmentParent<DataLoggerFragment> {
    OneSheeldTextView loggerStatus;
    LinearLayout keysContainer, valuesContainer;
    float scale = 0;
    LinearLayout.LayoutParams cellParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.data_logger_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((DataLoggerShield) getApplication().getRunningShields().get(
                getControllerTag())).setEventHandler(eventHandler);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        uiHandler = new Handler();
        loggerStatus = (OneSheeldTextView) v.findViewById(R.id.loggerStatus);
        keysContainer = (LinearLayout) v.findViewById(R.id.keysContainer);
        valuesContainer = (LinearLayout) v.findViewById(R.id.valuesContainer);
        scale = getResources().getDisplayMetrics().density;
        cellParams = new LinearLayout.LayoutParams((int) (100 * scale + .5f),
                LinearLayout.LayoutParams.MATCH_PARENT);
        int status = ((DataLoggerShield) getApplication().getRunningShields()
                .get(getControllerTag())).currentStatus;
        loggerStatus
                .setBackgroundResource(status == DataLoggerShield.READ_FOR_LOGGING ? R.drawable.large_yellow_circle
                        : R.drawable.large_green_circle);
        loggerStatus
                .setText(status == DataLoggerShield.READ_FOR_LOGGING ? R.string.readyToLog
                        : R.string.logging);
        keysContainer.removeAllViews();
        valuesContainer.removeAllViews();
    }
    DataLoggerListener eventHandler = new DataLoggerListener() {

        @Override
        public void onStopLogging(
                final ArrayList<Map<String, String>> loggedValues) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        loggerStatus
                                .setBackgroundResource(R.drawable.large_red_circle);
                        loggerStatus.setText(R.string.stopLogging);
                        ((DataLoggerShield) getApplication()
                                .getRunningShields().get(getControllerTag())).currentStatus = DataLoggerShield.STOPPED_LOGGING;
                        loggerStatus.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (canChangeUI()
                                        && !((DataLoggerShield) getApplication()
                                        .getRunningShields().get(
                                                getControllerTag()))
                                        .isLoggingStarted()) {
                                    keysContainer.removeAllViews();
                                    valuesContainer.removeAllViews();
                                    ((DataLoggerShield) getApplication()
                                            .getRunningShields().get(
                                                    getControllerTag())).currentStatus = DataLoggerShield.READ_FOR_LOGGING;
                                    loggerStatus
                                            .setBackgroundResource(R.drawable.large_yellow_circle);
                                    loggerStatus.setText(R.string.readyToLog);
                                }
                            }
                        }, 1000);
                    }
                }
            });
        }

        @Override
        public void onStartLogging() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        keysContainer.removeAllViews();
                        valuesContainer.removeAllViews();
                        loggerStatus
                                .setBackgroundResource(R.drawable.large_green_circle);
                        loggerStatus.setText(R.string.logging);
                    }
                }
            });
        }

        @Override
        public void onReadyForLogging() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        loggerStatus
                                .setBackgroundResource(R.drawable.large_yellow_circle);
                        loggerStatus.setText(R.string.readyToLog);
                    }
                }
            });
        }

        @Override
        public void onLog(final Map<String, String> rowData) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        loggerStatus
                                .setBackgroundResource(R.drawable.large_green_circle);
                        loggerStatus.setText(R.string.logging);
                        for (String header : rowData.keySet()) {
                            if (keysContainer.findViewWithTag(header) != null) {
                                ((OneSheeldTextView) valuesContainer
                                        .findViewWithTag(header + "Value"))
                                        .setText("");
                            } else {
                                OneSheeldTextView key = new OneSheeldTextView(
                                        activity);
                                key.setLayoutParams(cellParams);
                                key.setSingleLine(true);
                                key.setText(header);
                                key.setTextColor(getResources().getColor(
                                        R.color.offWhite));
                                key.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                                key.setGravity(Gravity.CENTER);
                                key.setTag(header);
                                key.setBackgroundResource(R.drawable.squared_data_logger_cell_borded);
                                OneSheeldTextView value = new OneSheeldTextView(
                                        activity);
                                value.setLayoutParams(cellParams);
                                value.setSingleLine(true);
                                value.setTextColor(getResources().getColor(
                                        R.color.offWhite));
                                value.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                                        14);
                                value.setBackgroundResource(R.drawable.squared_data_logger_cell_borded);
                                value.setGravity(Gravity.CENTER);
                                value.setTag(header + "Value");
                                keysContainer.addView(key);
                                valuesContainer.addView(value);
                            }
                        }
                    }
                }
            });

        }

        void add(String header, String valueT) {
            if (keysContainer.findViewWithTag(header) != null) {
                ((OneSheeldTextView) valuesContainer.findViewWithTag(header
                        + "Value")).setText(valueT);
            } else {
                OneSheeldTextView key = new OneSheeldTextView(activity);
                key.setLayoutParams(cellParams);
                key.setSingleLine(true);
                key.setText(header);
                key.setTextColor(getResources().getColor(R.color.offWhite));
                key.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                key.setGravity(Gravity.CENTER);
                key.setTag(header);
                key.setBackgroundResource(R.drawable.squared_data_logger_cell_borded);
                OneSheeldTextView value = new OneSheeldTextView(activity);
                value.setLayoutParams(cellParams);
                value.setSingleLine(true);
                value.setText(valueT);
                value.setTextColor(getResources().getColor(R.color.offWhite));
                value.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                value.setBackgroundResource(R.drawable.squared_data_logger_cell_borded);
                value.setGravity(Gravity.CENTER);
                value.setTag(header + "Value");
                keysContainer.addView(key);
                valuesContainer.addView(value);
            }
        }

        @Override
        public void onAdd(final String header, final String valueT) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        add(header, valueT);
                    }
                }
            });
        }
    };

}
