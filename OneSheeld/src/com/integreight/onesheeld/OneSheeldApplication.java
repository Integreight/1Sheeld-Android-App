package com.integreight.onesheeld;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.ControllerParent;

/**
 * @author SaadRoid
 * 
 */
public class OneSheeldApplication extends Application {
	private SharedPreferences appPreferences;
	private final String APP_PREF_NAME = "oneSheeldPreference";
	private final String LAST_DEVICE = "lastConnectedDevice";
	private final Hashtable<String, ControllerParent> runningSheelds = new Hashtable<String, ControllerParent>();
	private final List<OneSheeldServiceHandler> serviceEventHandlers = new ArrayList<OneSheeldServiceHandler>();
	private ArduinoFirmata appFirmata;
	private boolean isBoundService = false;
	private ConnectionDetector connectionHandler;

	@Override
	public void onCreate() {
		setAppPreferences(getSharedPreferences(APP_PREF_NAME, MODE_PRIVATE));
		setConnectionHandler(new ConnectionDetector(getApplicationContext()));
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

	public void addServiceEventHandler(
			OneSheeldServiceHandler serviceEventHandler) {
		if (!this.serviceEventHandlers.contains(serviceEventHandler))
			this.serviceEventHandlers.add(serviceEventHandler);
	}

	public List<OneSheeldServiceHandler> getServiceEventHandlers() {
		return serviceEventHandlers;
	}

	public void clearServiceEventHandlers(
			List<OneSheeldServiceHandler> serviceEventHandlers) {
		this.serviceEventHandlers.clear();
	}

	public ConnectionDetector getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(ConnectionDetector connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

}
