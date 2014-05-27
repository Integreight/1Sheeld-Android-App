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
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.GSMLocationupdates.SendFrameHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class GpsShield extends ControllerParent<GpsShield> implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, SendFrameHandler {
	public static final byte GPS_VALUE = 0x01;
	private GpsEventHandler eventHandler;
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private boolean mUpdatesRequested;
	private Location lastLocation;
	private ShieldFrame frame;
	private LocationManager manager;
	private static final int SERVICE_VERSION_UPDATE_REQUIRED = 2,
			SERVICE_MISSING = 1, SERVICE_DISABLED = 3, CANCELED = 13,
			SUCCESS = 0;

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
		manager = (LocationManager) getApplication().getSystemService(
				Context.LOCATION_SERVICE);
		// check if Google play services available, no dialog displayed
		if (isGoogleplayServicesAvailableNoDialogs()) {
			if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				startGps();
		}
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

		mLocationClient = new LocationClient(getActivity()
				.getApplicationContext(), this, this);
		if (mLocationClient != null)
			mLocationClient.connect();

	}

	public void isGooglePlayServicesAvailableWithDialog() {
		// checking if Google play services exist or not.
		if (isGooglePlayServicesAvailable()) {
			if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				buildAlertMessageNoGps();
			} else {
				startGps();
			}

		} else
			Log.d("Gps",
					"Google Play services was not available for some reasons");
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
			mLocationClient.removeLocationUpdates(this);
			mLocationClient.disconnect();
		}
		// After disconnect() is called, the client is considered "dead".
	}

	private void startPeriodicUpdates() {
		if (mLocationClient != null) {
			lastLocation = mLocationClient.getLastLocation();
			if (lastLocation != null) {
				Log.d("Gps Lat::Double", lastLocation.getLatitude() + "");
				Log.d("Gps Lat::Double in Radians",
						getRadians(lastLocation.getLatitude()) + "");
				Log.d("Gps Long::Double", lastLocation.getLongitude() + "");
				Log.d("Gps Long::Double in Radians",
						getRadians(lastLocation.getLongitude()) + "");
				if (eventHandler != null) {
					eventHandler
							.onLangChanged(lastLocation.getLongitude() + "");
					eventHandler.onLatChanged(lastLocation.getLatitude() + "");
				}
			} /*
			 * else {
			 * 
			 * GSMLocationupdates gsmLocationupdates = new GSMLocationupdates(
			 * 
			 * getApplication().getApplicationContext());
			 * gsmLocationupdates.updateLocation();
			 * 
			 * }
			 */
		}

	}

	private double getRadians(double angdeg) {
		double result = Math.toRadians(angdeg);
		return result;

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
			// showErrorDialog(SERVICE_DISABLED);
			return false;
		case SERVICE_MISSING:
			// showErrorDialog(SERVICE_MISSING);
			return false;
		case SERVICE_VERSION_UPDATE_REQUIRED:
			// showErrorDialog(SERVICE_VERSION_UPDATE_REQUIRED);
			return false;
		case CANCELED:
			// showErrorDialog(CANCELED);
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
			eventHandler.onLangChanged(arg0.getLongitude() + "");
			eventHandler.onLatChanged(arg0.getLatitude() + "");
		}
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
		if (mUpdatesRequested && mLocationRequest != null
				&& mLocationClient != null) {
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
		frame = new ShieldFrame(UIShield.GPS_SHIELD.getId(), GPS_VALUE);
		// frame.addByteArgument((byte) Math.round(event.values[0]));
		float lat = (float) myLocation.getLatitude();// getRadians(myLocation.getLatitude());
		float lang = (float) myLocation.getLongitude();// getRadians(myLocation.getLongitude());
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
}
