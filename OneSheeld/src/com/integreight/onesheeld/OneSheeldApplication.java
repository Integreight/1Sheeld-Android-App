package com.integreight.onesheeld;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
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
	private Hashtable<String, ControllerParent<?>> runningSheelds = new Hashtable<String, ControllerParent<?>>();
	private final List<OneSheeldServiceHandler> serviceEventHandlers = new ArrayList<OneSheeldServiceHandler>();
	private ArduinoFirmata appFirmata;
	private ConnectionDetector connectionHandler;
	private ArduinoFirmataEventHandler arduinoFirmataEventHandler,
			arduinoFirmataHandlerForConnectivityPopup;

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

	public Hashtable<String, ControllerParent<?>> getRunningShields() {
		return runningSheelds;
	}

	public void setRunningSheelds(
			Hashtable<String, ControllerParent<?>> runningSheelds) {
		this.runningSheelds = runningSheelds;
	}

	public ArduinoFirmata getAppFirmata() {

		return appFirmata;
	}

	public void setAppFirmata(ArduinoFirmata appFirmata) {
		this.appFirmata = appFirmata;
		this.appFirmata.addEventHandler(arduinoFirmataEventHandler);
		this.appFirmata
				.addEventHandler(arduinoFirmataHandlerForConnectivityPopup);
	}

	public void addServiceEventHandler(
			OneSheeldServiceHandler serviceEventHandler) {
		if (!this.serviceEventHandlers.contains(serviceEventHandler))
			this.serviceEventHandlers.add(serviceEventHandler);
	}

	public List<OneSheeldServiceHandler> getServiceEventHandlers() {
		return serviceEventHandlers;
	}

	public void clearServiceEventHandlers() {
		if (getAppFirmata() != null) {
			getRunningShields().clear();
			getAppFirmata().clearArduinoFirmataDataHandlers();
			getAppFirmata().clearArduinoFirmataShieldFrameHandlers();
		}
	}

	public ConnectionDetector getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(ConnectionDetector connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	public ArduinoFirmataEventHandler getArduinoFirmataEventHandler() {
		return arduinoFirmataEventHandler;
	}

	public void setArduinoFirmataEventHandler(
			ArduinoFirmataEventHandler arduinoFirmataEventHandler) {
		this.arduinoFirmataEventHandler = arduinoFirmataEventHandler;
	}

	public ArduinoFirmataEventHandler getArduinoFirmataHandlerForConnectivityPopup() {
		return arduinoFirmataHandlerForConnectivityPopup;
	}

	public void setArduinoFirmataHandlerForConnectivityPopup(
			ArduinoFirmataEventHandler arduinoFirmataHandlerForConnectivityPopup) {
		this.arduinoFirmataHandlerForConnectivityPopup = arduinoFirmataHandlerForConnectivityPopup;
	}

	// public ServiceConnection getmConnection() {
	// return mConnection;
	// }
	//
	// private final ServiceConnection mConnection = new ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName className, IBinder service)
	// {
	// // We've bound to LocalService, cast the IBinder and get
	// // LocalService instance
	// // OneSheeldBinder binder = (OneSheeldBinder) service;
	// setAppFirmata(binder.getService().getFirmata());
	// getAppFirmata().addEventHandler(getArduinoFirmataEventHandler());
	// for (OneSheeldServiceHandler serviceHandler : getServiceEventHandlers())
	// {
	// serviceHandler.onSuccess(getAppFirmata());
	// }
	// // isBoundService = true;
	//
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName arg0) {
	// for (OneSheeldServiceHandler serviceHandler : getServiceEventHandlers())
	// {
	// serviceHandler.onFailure();
	// }
	// // isBoundService = false;
	// }
	// };
}
