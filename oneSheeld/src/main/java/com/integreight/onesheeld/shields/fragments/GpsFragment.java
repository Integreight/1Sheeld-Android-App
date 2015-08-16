package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GpsShield;
import com.integreight.onesheeld.shields.controller.GpsShield.GpsEventHandler;
import com.integreight.onesheeld.utils.Log;

public class GpsFragment extends ShieldFragmentParent<GpsFragment> {
    TextView Latit, Longit;
    Button startGps, stopGps;
    String lat;
    String lng;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gps_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnStart() {
        ((GpsShield) getApplication().getRunningShields().get(
                getControllerTag())).setGpsEventHandler(gpsEventHandler);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        Latit = (TextView) v.findViewById(R.id.lat_value_txt);
        Longit = (TextView) v.findViewById(R.id.lang_value_txt);
        startGps = (Button) v.findViewById(R.id.start_listener_bt);
        stopGps = (Button) v.findViewById(R.id.stop_listener_bt);

        startGps.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((GpsShield) getApplication().getRunningShields().get(
                        getControllerTag()))
                        .isGooglePlayServicesAvailableWithDialog();

            }
        });

        stopGps.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((GpsShield) getApplication().getRunningShields().get(
                        getControllerTag())).stopGps();

            }
        });
    }

    private GpsEventHandler gpsEventHandler = new GpsEventHandler() {

        @Override
        public void onLatChanged(final String lat) {
            // TODO Auto-generated method stub
            if (lat != null && lat.length() > 0)
                Latit.post(new Runnable() {

                    @Override
                    public void run() {
                        GpsFragment.this.lat = lat;
                        if (canChangeUI())
                            Latit.setText("Latitude\n" + lat);
                    }
                });
        }

        @Override
        public void onLangChanged(final String lang) {
            // TODO Auto-generated method stub
            if (lang != null && lang.length() > 0)
                Longit.post(new Runnable() {

                    @Override
                    public void run() {
                        GpsFragment.this.lng = lng;
                        if (canChangeUI())
                            Longit.setText("Longitude\n" + lang);
                    }
                });
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new GpsShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

    ;

    @Override
    public void doOnResume() {
        ((GpsShield) getApplication().getRunningShields().get(
                getControllerTag())).isGooglePlayServicesAvailableWithDialog();
        if (((GpsShield) getApplication().getRunningShields().get(
                getControllerTag())).getLastKnownLocation() != null) {
            lat = ((GpsShield) getApplication().getRunningShields().get(
                    getControllerTag())).getLastKnownLocation().getLatitude() + "";
            lng = ((GpsShield) getApplication().getRunningShields().get(
                    getControllerTag())).getLastKnownLocation().getLongitude() + "";
        }
        if (lat != null && lng != null && canChangeUI()) {
            Latit.setText("Latitude\n" + lat);
            Longit.setText("Longitude\n" + lng);

        }

    }

}
