package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GSMLocationupdates implements LocationListener {
	private LocationManager locManager;
	private SendFrameHandler frameHandler;

	public GSMLocationupdates(Context context) {
		locManager = (LocationManager) context.getApplicationContext()
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public void updateLocation() {
		if (locManager != null) {
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, this);
			/*
			 * Location location = locManager
			 * .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			 */
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null && frameHandler != null)
			frameHandler.sendFrameHandler(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Location location = null;
		if (frameHandler != null)
			frameHandler.sendFrameHandler(location);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Location location = null;
		if (frameHandler != null)
			frameHandler.sendFrameHandler(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Location location = null;

		if (extras != null && frameHandler != null)
			frameHandler.sendFrameHandler(location);
	}

	public interface SendFrameHandler {
		void sendFrameHandler(Location location);
	}
}
