package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.SendFrameHandler;
import com.integreight.onesheeld.utils.Log;

public class GpsShield extends ControllerParent<GpsShield> implements
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SendFrameHandler {
    public static final byte GPS_VALUE = 0x01;
    private GpsEventHandler eventHandler;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mLocationClient;
    private boolean mUpdatesRequested;
    private ShieldFrame frame;
    private LocationManager manager;
    private static final int SERVICE_VERSION_UPDATE_REQUIRED = 2,
            SERVICE_MISSING = 1, SERVICE_DISABLED = 3, CANCELED = 13,
            SUCCESS = 0;
    private Location lastKnownLocation;

    int PERIOD = 5000;

    public GpsShield() {
    }

    public GpsShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<GpsShield> init(String tag) {
        mLocationClient = new GoogleApiClient.Builder(getApplication())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mUpdatesRequested = false;
        manager = (LocationManager) getApplication().getSystemService(
                Context.LOCATION_SERVICE);
        // check if Google play services available, no dialog displayed
        if (isGoogleplayServicesAvailableNoDialogs()) {
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                startGps();
        }
        return super.init(tag);
    }

    public void setGpsEventHandler(GpsEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public void startGps() {
        mUpdatesRequested = true;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(PERIOD);
        mLocationRequest.setFastestInterval(PERIOD);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // check Internet connection

        mLocationClient = new GoogleApiClient.Builder(getApplication())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (mLocationClient != null)
            mLocationClient.connect();

    }

    public void isGooglePlayServicesAvailableWithDialog() {
        // checking if Google play services exist or not.
        if (mLocationClient != null && !mLocationClient.isConnected()) {
            if (isGooglePlayServicesAvailable()) {
                if (!manager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    startGps();
                }

            } else
                Log.d("Gps",
                        "Google Play services was not available for some reasons");
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(
                "Please Use wirless networks to determine your location !")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                getActivity()
                                        .startActivity(
                                                new Intent(
                                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void stopGps() {
        mUpdatesRequested = false;
        if (mLocationClient.isConnected()) {
            mLocationClient.unregisterConnectionCallbacks(this);
            mLocationClient.disconnect();
        }
    }

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                getActivity(), 0);

        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(
                    ((MainActivity) getActivity()).getSupportFragmentManager(),
                    "Gps Sheeld");
        }
    }

    private boolean isGooglePlayServicesAvailable() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()
                        .getApplicationContext());

        switch (resultCode) {
            case SUCCESS:
                return true;
            case SERVICE_DISABLED:
                showErrorDialog(SERVICE_DISABLED);
                return false;
            case SERVICE_MISSING:
                showErrorDialog(SERVICE_MISSING);
                return false;
            case SERVICE_VERSION_UPDATE_REQUIRED:
                showErrorDialog(SERVICE_VERSION_UPDATE_REQUIRED);
                return false;
            case CANCELED:
                showErrorDialog(CANCELED);
                return false;

        }
        return false;

    }

    private boolean isGoogleplayServicesAvailableNoDialogs() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()
                        .getApplicationContext());

        switch (resultCode) {
            case SUCCESS:
                return true;
            case SERVICE_DISABLED:
                return false;
            case SERVICE_MISSING:
                return false;
            case SERVICE_VERSION_UPDATE_REQUIRED:
                return false;
            case CANCELED:
                return false;

        }
        return false;

    }

    public interface GpsEventHandler {
        void onLangChanged(String lang);

        void onLatChanged(String lat);

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        stopGps();
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
        if (eventHandler != null && mLocationClient.isConnected()) {
            lastKnownLocation = arg0;
            eventHandler.onLangChanged(arg0.getLongitude() + "");
            eventHandler.onLatChanged(arg0.getLatitude() + "");
        }
//		lastLocation = arg0;
        sendFrame(arg0);
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        Log.d("GPS Location Failed", arg0.toString());

    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        if (mUpdatesRequested && mLocationRequest != null
                && mLocationClient != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mLocationClient, mLocationRequest, this);
        }

    }

    private void sendFrame(Location myLocation) {
        // TODO Auto-generated method stub
        frame = new ShieldFrame(UIShield.GPS_SHIELD.getId(), GPS_VALUE);
        float lat = (float) myLocation.getLatitude();
        float lang = (float) myLocation.getLongitude();
        frame.addFloatArgument(lat);
        frame.addFloatArgument(lang);
        sendShieldFrame(frame);

    }

    @Override
    public void sendFrameHandler(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (eventHandler != null) {
                eventHandler.onLangChanged(latitude + "");
                eventHandler.onLatChanged(longitude + "");
            }
            sendFrame(location);
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

    }
}
