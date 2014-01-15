package com.integreight.onesheeld;

import java.util.Hashtable;

import android.app.Application;
import android.content.SharedPreferences;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.utils.ControllerParent;

public class OneSheeldApplication extends Application {
	private SharedPreferences appPreferences;
	private final String APP_PREF_NAME = "oneSheeldPreference";
	private final String LAST_DEVICE = "lastConnectedDevice";
	private final Hashtable<String, ControllerParent> runningSheelds = new Hashtable<String, ControllerParent>();
	private ArduinoFirmata appFirmata;
	private boolean isBoundService = false;

	@Override
	public void onCreate() {
		setAppPreferences(getSharedPreferences(APP_PREF_NAME, MODE_PRIVATE));
		setAppFirmata(new ArduinoFirmata(getApplicationContext()));
		super.onCreate();
	}

	public SharedPreferences getAppPreferences() {
		return appPreferences;
	}

	public void setAppPreferences(SharedPreferences appPreferences) {
		this.appPreferences = appPreferences;
	}

	public String getLastConnectedDevice() {
		return appPreferences.getString(LAST_DEVICE, null);
	}

	public void setLastConnectedDevice(String lastConnectedDevice) {
		appPreferences.edit().putString(LAST_DEVICE, lastConnectedDevice)
				.commit();
	}

	public String getAPP_PREF_NAME() {
		return APP_PREF_NAME;
	}

	public String getLAST_DEVICE() {
		return LAST_DEVICE;
	}

	public Hashtable<String, ControllerParent> getRunningSheelds() {
		return runningSheelds;
	}

	public ArduinoFirmata getAppFirmata() {

		return appFirmata;
	}

	public void setAppFirmata(ArduinoFirmata appFirmata) {
		this.appFirmata = appFirmata;
	}

	public boolean isBoundService() {
		return isBoundService;
	}

	public void setBoundService(boolean isBoundService) {
		this.isBoundService = isBoundService;
	}

}
