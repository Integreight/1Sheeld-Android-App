package com.integreight.onesheeld;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.util.SparseArray;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.RemoteOneSheeldShield;
import com.integreight.onesheeld.shields.controller.TaskerShield;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

/**
 * @author Ahmed Saad
 * 
 */
public class OneSheeldApplication extends Application {
	private SharedPreferences appPreferences;
	public static int ARDUINO_LIBRARY_VERSION = 4;
	private final String APP_PREF_NAME = "oneSheeldPreference";
	private final String LAST_DEVICE = "lastConnectedDevice";
	private final String MAJOR_VERSION = "majorVersion";
	private final String MINOR_VERSION = "minorVersion";
	private final String VERSION_WEB_RESULT = "versionWebResult";
	private final String BUZZER_SOUND_KEY = "buzerSound";
	private final String TUTORIAL_SHOWN_TIME = "tutShownTime";
	private final String SHOWTUTORIALS_AGAIN = "showTutAgain";
	private final String TASKER_CONDITION_PIN = "taskerConditionPin";
	private final String TASKER_CONDITION_STATUS = "taskerConditionStatus";
	private Hashtable<String, ControllerParent<?>> runningSheelds = new Hashtable<String, ControllerParent<?>>();
	private final List<OneSheeldServiceHandler> serviceEventHandlers = new ArrayList<OneSheeldServiceHandler>();
	public static final Hashtable<String, String> shieldsFragmentsTags = new Hashtable<String, String>();
	private ArduinoFirmata appFirmata;
	private ConnectionDetector connectionHandler;
	private ArduinoFirmataEventHandler arduinoFirmataEventHandler;
	public Typeface appFont;
	// private GoogleAnalytics googleAnalyticsInstance;
	// private Tracker appGaTracker;
	public ApiObjects socialKeys = new ApiObjects();
	public TaskerShield taskerController;
	public RemoteOneSheeldShield remoteOneSheeldController;
	public SparseArray<Boolean> taskerPinsStatus;

	public static final String FIRMWARE_UPGRADING_URL = "http://1sheeld.parseapp.com/firmware/version.json";

	private static boolean isDebuggable = true;

	private Tracker gaTracker;

	private long connectionTime;

	/*
	 * Google Analytics configuration values.
	 */
	// Placeholder property ID.
	// private static final String GA_PROPERTY_ID = "";
	//
	// // Dispatch period in seconds.
	// private static final int GA_DISPATCH_PERIOD = 30;

	// // Prevent hits from being sent to reports, i.e. during testing.
	// private static final boolean GA_IS_DRY_RUN = false;
	//
	// // GA Logger verbosity.
	// private static final LogLevel GA_LOG_VERBOSITY = LogLevel.VERBOSE;
	//
	// // Key used to store a user's tracking preferences in SharedPreferences.
	// private static final String TRACKING_PREF_KEY = "trackingPreference";

	// public GoogleAnalytics getGoogleAnalytics() {
	// if (googleAnalyticsInstance == null) {
	// googleAnalyticsInstance = GoogleAnalytics
	// .getInstance(getApplicationContext());
	// }
	// return googleAnalyticsInstance;
	// }
	//
	// @SuppressWarnings("deprecation")
	// public Tracker getGaTracker() {
	// if (appGaTracker == null) {
	// appGaTracker = getGoogleAnalytics().getTracker(GA_PROPERTY_ID);
	//
	// // Set dispatch period.
	// GAServiceManager.getInstance().setLocalDispatchPeriod(
	// GA_DISPATCH_PERIOD);
	//
	// // Set dryRun flag.
	// googleAnalyticsInstance.setDryRun(GA_IS_DRY_RUN);
	// // Set Logger verbosity.
	// googleAnalyticsInstance.getLogger().setLogLevel(GA_LOG_VERBOSITY);
	// // Set the opt out flag when user updates a tracking preference.
	// SharedPreferences userPrefs = PreferenceManager
	// .getDefaultSharedPreferences(this);
	// userPrefs
	// .registerOnSharedPreferenceChangeListener(new
	// SharedPreferences.OnSharedPreferenceChangeListener() {
	// @Override
	// public void onSharedPreferenceChanged(
	// SharedPreferences sharedPreferences, String key) {
	// if (key.equals(TRACKING_PREF_KEY)) {
	// GoogleAnalytics.getInstance(
	// getApplicationContext()).setAppOptOut(
	// sharedPreferences
	// .getBoolean(key, false));
	// }
	// }
	// });
	// }
	// return appGaTracker;
	// }

	public void startConnectionTimer() {
		connectionTime = System.currentTimeMillis();
	}

	public void endConnectionTimerAndReport() {
		if (connectionTime == 0)
			return;
		Map<String, String> hit = new HitBuilders.TimingBuilder()
				.setCategory("Connection Timing")
				.setValue(System.currentTimeMillis() - connectionTime)
				.setVariable("Connection").build();
		// hit.put("&sc", "end");
		getTracker().send(hit);
		connectionTime = 0;
	}

	public long getConnectionTime() {
		return connectionTime;
	}

	public synchronized Tracker getTracker() {
		if (gaTracker != null)
			return gaTracker;
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		analytics.setAppOptOut(isDebuggable);
		if (isDebuggable)
			analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
		gaTracker = analytics.newTracker(ApiObjects.analytics
				.get("property_id"));
		gaTracker.enableAdvertisingIdCollection(true);
		gaTracker.setSessionTimeout(-1);
		return gaTracker;
	}

	public static boolean isDebuggable() {
		return isDebuggable;
	}

	@Override
	public void onCreate() {
		setAppPreferences(getSharedPreferences(APP_PREF_NAME, MODE_PRIVATE));
		setConnectionHandler(new ConnectionDetector());
		appFont = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		setAppFirmata(new ArduinoFirmata(getApplicationContext()));
		parseSocialKeys();
		Parse.initialize(this, ApiObjects.parse.get("app_id"),
				ApiObjects.parse.get("client_id"));
		ParseInstallation.getCurrentInstallation().saveInBackground();
		initTaskerPins();
		isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		if (isDebuggable()
				&& (ParseInstallation.getCurrentInstallation().getList(
						"channels") == null || !ParseInstallation
						.getCurrentInstallation().getList("channels")
						.contains("dev")))
			ParsePush.subscribeInBackground("dev");
		connectionTime = 0;
        AppShields.getInstance().init();
		super.onCreate();
	}

	@SuppressLint("UseSparseArrays")
	public void initTaskerPins() {
		ArduinoPin[] pins = ArduinoPin.values();
		taskerPinsStatus = new SparseArray<Boolean>(pins.length);
		for (ArduinoPin pin : pins) {
			taskerPinsStatus.put(pin.microHardwarePin, false);
		}
	}

	private void parseSocialKeys() {
		String metaData = "";
		try {
			metaData = loadData("dev_meta_data.json");
		} catch (Exception e) {
			try {
				metaData = loadData("meta_data.json");
			} catch (Exception e1) {
			}
		}
		try {
			JSONObject socialKeysObject = new JSONObject(metaData);
			JSONObject facebook = new JSONObject();
			JSONObject twitter = new JSONObject();
			JSONObject foursquare = new JSONObject();
			JSONObject parse = new JSONObject();
			JSONObject analytics = new JSONObject();
			if (socialKeysObject.has("facebook")) {
				facebook = socialKeysObject.getJSONObject("facebook");
				if (facebook.has("app_id"))
					ApiObjects.facebook.add("app_id",
							facebook.getString("app_id"));
			}
			if (socialKeysObject.has("twitter")) {
				twitter = socialKeysObject.getJSONObject("twitter");
				if (twitter.has("consumer_key")
						&& twitter.has("consumer_secret")) {
					ApiObjects.twitter.add("consumer_key",
							twitter.getString("consumer_key"));
					ApiObjects.twitter.add("consumer_secret",
							twitter.getString("consumer_secret"));
				}
			}
			if (socialKeysObject.has("foursquare")) {
				foursquare = socialKeysObject.getJSONObject("foursquare");
				if (foursquare.has("client_key")
						&& foursquare.has("client_secret")) {
					ApiObjects.foursquare.add("client_key",
							foursquare.getString("client_key"));
					ApiObjects.foursquare.add("client_secret",
							foursquare.getString("client_secret"));
				}
			}
			if (socialKeysObject.has("parse")) {
				parse = socialKeysObject.getJSONObject("parse");
				if (parse.has("app_id") && parse.has("client_id"))
					ApiObjects.parse.add("app_id", parse.getString("app_id"));
				ApiObjects.parse.add("client_id", parse.getString("client_id"));
			}
			if (socialKeysObject.has("analytics")) {
				analytics = socialKeysObject.getJSONObject("analytics");
				if (analytics.has("property_id"))
					ApiObjects.analytics.add("property_id",
							analytics.getString("property_id"));
			}
		} catch (JSONException e) {
		}
	}

	public String loadData(String inFile) throws IOException {
		String tContents = "";
		InputStream stream = getAssets().open(inFile);
		int size = stream.available();
		byte[] buffer = new byte[size];
		stream.read(buffer);
		stream.close();
		tContents = new String(buffer);
		return tContents;
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

	public int getMajorVersion() {
		return appPreferences.getInt(MAJOR_VERSION, -1);
	}

	public void setMajorVersion(int majorVersion) {
		appPreferences.edit().putInt(MAJOR_VERSION, majorVersion).commit();
	}

	public int getMinorVersion() {
		return appPreferences.getInt(MINOR_VERSION, -1);
	}

	public void setMinorVersion(int minorVersion) {
		appPreferences.edit().putInt(MINOR_VERSION, minorVersion).commit();
	}

	public void setVersionWebResult(String json) {
		appPreferences.edit().putString(VERSION_WEB_RESULT, json).commit();
	}

	public String getVersionWebResult() {
		return appPreferences.getString(VERSION_WEB_RESULT, null);
	}

	public void setBuzzerSound(String uri) {
		appPreferences.edit().putString(BUZZER_SOUND_KEY, uri).commit();
	}

	public String getBuzzerSound() {
		return appPreferences.getString(BUZZER_SOUND_KEY, null);
	}

	public void setTutShownTimes(int times) {
		appPreferences.edit().putInt(TUTORIAL_SHOWN_TIME, times).commit();
	}

	public int getTutShownTimes() {
		return appPreferences.getInt(TUTORIAL_SHOWN_TIME, 0);
	}

	public void setTaskerConditionPin(int pin) {
		appPreferences.edit().putInt(TASKER_CONDITION_PIN, pin).commit();
	}

	public int getTaskerConditionPin() {
		return appPreferences.getInt(TASKER_CONDITION_PIN, -1);
	}

	public void setTaskerConditionStatus(boolean flag) {
		appPreferences.edit().putBoolean(TASKER_CONDITION_STATUS, flag)
				.commit();
	}

	public boolean getTaskConditionStatus() {
		return appPreferences.getBoolean(TASKER_CONDITION_STATUS, true);
	}

	public void setShownTutAgain(boolean flag) {
		appPreferences.edit().putBoolean(SHOWTUTORIALS_AGAIN, !flag).commit();
	}

	public boolean getShowTutAgain() {
		return appPreferences.getBoolean(SHOWTUTORIALS_AGAIN, true);
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
			// getRunningShields().clear();
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
		getAppFirmata().addEventHandler(arduinoFirmataEventHandler);
	}

	// public ArduinoFirmataEventHandler
	// getArduinoFirmataHandlerForConnectivityPopup() {
	// return arduinoFirmataHandlerForConnectivityPopup;
	// }
	//
	// public void setArduinoFirmataHandlerForConnectivityPopup(
	// ArduinoFirmataEventHandler arduinoFirmataHandlerForConnectivityPopup) {
	// this.arduinoFirmataHandlerForConnectivityPopup =
	// arduinoFirmataHandlerForConnectivityPopup;
	// }
}
