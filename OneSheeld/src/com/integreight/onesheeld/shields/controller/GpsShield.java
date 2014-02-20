package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import com.integreight.onesheeld.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class GpsShield extends ControllerParent<GpsShield> implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private GpsEventHandler eventHandler;
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private boolean mUpdatesRequested;
	private Location lastLocation;
	private ShieldFrame frame;

	Handler handler;
	int PERIOD = 5000;

	public GpsShield() {
	}

	public GpsShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<GpsShield> setTag(String tag) {

		mLocationClient = new LocationClient(getActivity()
				.getApplicationContext(), this, this);
		mUpdatesRequested = false;
		return super.setTag(tag);
	}

	public void setGpsEventHandler(GpsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	public void startGps() {
		mUpdatesRequested = true;
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(PERIOD);
		mLocationRequest.setFastestInterval(PERIOD);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocationClient = new LocationClient(getActivity()
				.getApplicationContext(), this, this);
		mLocationClient.connect();

	}

	public void stopGps() {
		mUpdatesRequested = false;
		if (servicesConnected() && mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();
	}

	private void startPeriodicUpdates() {
		if (servicesConnected()) {
			lastLocation = mLocationClient.getLastLocation();
			if (lastLocation != null) {
				Log.d("Gps Lat::Double", lastLocation.getLatitude() + "");
				Log.d("Gps Lat::Double in Radians",
						getRadians(lastLocation.getLatitude()) + "");
				Log.d("Gps Long::Double", lastLocation.getLongitude() + "");
				Log.d("Gps Long::Double in Radians",
						getRadians(lastLocation.getLongitude()) + "");
				eventHandler.onLangChanged(lastLocation.getLongitude() + "");
				eventHandler.onLatChanged(lastLocation.getLatitude() + "");
			}
		}

	}

	private double getRadians(double angdeg) {
		double result = Math.toRadians(angdeg);
		return result;

	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity()
						.getApplicationContext());

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			return false;
		}
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
		eventHandler.onLangChanged(arg0.getLongitude() + "");
		eventHandler.onLatChanged(arg0.getLatitude() + "");
		lastLocation = arg0;
		sendFrame(arg0);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Log.d("GPS Location Failed", arg0.toString());

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		if (mUpdatesRequested && servicesConnected()) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			startPeriodicUpdates();
		}

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	private void sendFrame(Location myLocation) {
		// TODO Auto-generated method stub
		frame = new ShieldFrame(UIShield.GPS_SHIELD.getId(), (byte) 0,
				ShieldFrame.DATA_SENT);
		// frame.addByteArgument((byte) Math.round(event.values[0]));
		float lat = (float)myLocation.getLatitude();// getRadians(myLocation.getLatitude());
		float lang = (float)myLocation.getLongitude();// getRadians(myLocation.getLongitude());
		frame.addFloatArgument(lat);
		frame.addFloatArgument(lang);
		activity.getThisApplication().getAppFirmata().sendShieldFrame(frame);

	}

}
