package com.integreight.onesheeld;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.SystemClock;
import android.util.SparseArray;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.TaskerShield;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.HttpRequest;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;

/**
 * @author Ahmed Saad
 */
public class OneSheeldApplication extends Application {
    private SharedPreferences appPreferences;
    public static int ARDUINO_LIBRARY_VERSION = 17;
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
    private final String CAMERA_CAPTURING = "cameraCapturing";
    private final String REMEMBER_SHIELDS = "rememberedShields";
    private OneSheeldDevice connectedDevice;
    private Hashtable<String, ControllerParent<?>> runningSheelds = new Hashtable<String, ControllerParent<?>>();
    public Typeface appFont;
    // private GoogleAnalytics googleAnalyticsInstance;
    // private Tracker appGaTracker;
    public TaskerShield taskerController;
    public SparseArray<Boolean> taskerPinsStatus;
    private boolean isLocatedInTheUs = false;

    public static final String FIRMWARE_UPGRADING_URL = "https://raw.githubusercontent.com/Integreight/1Sheeld-Firmware/master/version.json";

    private static boolean isDebuggable = true;

    private static boolean isDemoMode = false;

    private Tracker gaTracker;

    private long connectionTime;

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public void startConnectionTimer() {
        connectionTime = SystemClock.elapsedRealtime();
    }

    public void endConnectionTimerAndReport() {
        if (connectionTime == 0)
            return;
        Map<String, String> hit = new HitBuilders.TimingBuilder()
                .setCategory("Connection Timing")
                .setValue(SystemClock.elapsedRealtime() - connectionTime)
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
        OneSheeldSdk.init(this);
        context = getApplicationContext();
        setAppPreferences(getSharedPreferences(APP_PREF_NAME, MODE_PRIVATE));
        appFont = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        parseSocialKeys();
        initTaskerPins();
        isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (isDebuggable() && !FirebaseApp.getApps(this).isEmpty())
            FirebaseMessaging.getInstance().subscribeToTopic("dev");
        OneSheeldSdk.setDebugging(isDebuggable);
        connectionTime = 0;
        AppShields.getInstance().init(getRememberedShields());
        initCrashlyticsAndUncaughtThreadHandler();
        detectIfLocatedInTheUs();
        super.onCreate();
    }

    private void initCrashlyticsAndUncaughtThreadHandler() {
        Thread.UncaughtExceptionHandler myHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, final Throwable arg1) {
                arg1.printStackTrace();
                ArduinoConnectivityPopup.isOpened = false;
                MainActivity.thisInstance.moveTaskToBack(true);

                Enumeration<String> enumKey = getRunningShields().keys();
                while (enumKey.hasMoreElements()) {
                    String key = enumKey.nextElement();
                    getRunningShields().get(key).resetThis();
                    getRunningShields().remove(key);
                }
                OneSheeldSdk.getManager().disconnectAll();
                if (MainActivity.thisInstance != null)
                    MainActivity.thisInstance.stopService();
                Intent in = MainActivity.thisInstance != null ? new Intent(MainActivity.thisInstance.getIntent()) : new Intent();
                PendingIntent intent = PendingIntent
                        .getActivity(getBaseContext(), 0, in,
                                MainActivity.thisInstance != null ? MainActivity.thisInstance.getIntent().getFlags() : 0);

                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                            intent);
                } else {
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                            intent);
                }
                setTutShownTimes(
                        getTutShownTimes() + 1);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
//    public void setCameraCapturing(boolean flag) {
//        appPreferences.edit().putBoolean(CAMERA_CAPTURING, flag)
//                .commit();
//    }
//
//    public boolean isCameraCapturing() {
//        return appPreferences.getBoolean(CAMERA_CAPTURING, false);
//    }

    public void setShownTutAgain(boolean flag) {
        appPreferences.edit().putBoolean(SHOWTUTORIALS_AGAIN, !flag).commit();
    }

    public boolean getShowTutAgain() {
        return appPreferences.getBoolean(SHOWTUTORIALS_AGAIN, true);
    }

    public void setRememberedShields(String shields) {
        appPreferences.edit().putString(REMEMBER_SHIELDS, shields).commit();
        Toast.makeText(this, getString(shields == null || shields.trim().length() == 0 ? R.string.general_toasts_shields_selection_has_been_cleared_toast : R.string.general_toasts_shields_selection_has_been_saved_toast), Toast.LENGTH_SHORT).show();
    }

    public String getRememberedShields() {
        return appPreferences.getString(REMEMBER_SHIELDS, null);
    }

    public Hashtable<String, ControllerParent<?>> getRunningShields() {
        return runningSheelds;
    }

    public void setRunningSheelds(
            Hashtable<String, ControllerParent<?>> runningSheelds) {
        this.runningSheelds = runningSheelds;
    }

    public boolean getIsDemoMode() {
        return isDemoMode;
    }

    public void setIsDemoMode(boolean isDemoMode) {
        OneSheeldApplication.isDemoMode = isDemoMode;
    }

    public OneSheeldDevice getConnectedDevice() {
        return connectedDevice;
    }

    public void setConnectedDevice(OneSheeldDevice connectedDevice) {
//        if (this.connectedDevice != null) {
//            while (isConnectedToBluetooth())
//                this.connectedDevice.disconnect();
//        }
        this.connectedDevice = connectedDevice;
    }

    public boolean isConnectedToBluetooth() {
        return connectedDevice != null && connectedDevice.isConnected();
    }

    public static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.notification_icon : R.drawable.white_ee_icon;
    }

    private void detectIfLocatedInTheUs() {
        if (ConnectionDetector.isConnectingToInternet(this)) {
            HttpRequest.getInstance().get("http://ip-api.com/json/?fields=3", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response != null) {
                        if (response.has("countryCode")) try {
                            isLocatedInTheUs = response.getString("countryCode").toLowerCase().equals("us");
                        } catch (JSONException e) {
                        }
                    }
                }
            });
        }
    }

    public boolean isLocatedInTheUs(){
        return isLocatedInTheUs;
    }
}
