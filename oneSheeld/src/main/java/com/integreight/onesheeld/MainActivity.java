package com.integreight.onesheeld;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.integreight.firmatabluetooth.ArduinoLibraryVersionChangeHandler;
import com.integreight.firmatabluetooth.FirmwareVersionQueryHandler;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup.onConnectedToBluetooth;
import com.integreight.onesheeld.popup.FirmwareUpdatingPopup;
import com.integreight.onesheeld.popup.ValidationPopup;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity {
    public AppSlidingLeftMenu appSlidingMenu;
    public boolean isForground = false;
    private onConnectedToBluetooth onConnectToBlueTooth = null;
    public static String currentShieldTag = null;
    public static MainActivity thisInstance;
    private boolean isBackPressed = false;

    public OneSheeldApplication getThisApplication() {
        return (OneSheeldApplication) getApplication();
    }

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        initCrashlyticsAndUncaughtThreadHandler();
        setContentView(R.layout.one_sheeld_main);
        initLooperThread();
        replaceCurrentFragment(R.id.appTransitionsContainer,
                SheeldsList.getInstance(), "base", true, false);
        resetSlidingMenu();
        if (getThisApplication().getAppFirmata() != null) {
            getThisApplication().getAppFirmata()
                    .addFirmwareVersionQueryHandler(versionChangingHandler);
            getThisApplication().getAppFirmata()
                    .addArduinoLibraryVersionQueryHandler(
                            arduinoLibraryVersionHandler);
        }
        if (getThisApplication().getShowTutAgain()
                && getThisApplication().getTutShownTimes() < 6)
            startActivity(new Intent(this, Tutorial.class));
    }

    public Thread looperThread;
    public Handler backgroundThreadHandler;
    private Looper backgroundHandlerLooper;

    private void stopLooperThread() {
        if (looperThread != null && looperThread.isAlive()) {
            looperThread.interrupt();
            backgroundHandlerLooper.quit();
            looperThread = null;
        }
    }

    public void initLooperThread() {
        stopLooperThread();
        looperThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Looper.prepare();
                backgroundHandlerLooper = Looper.myLooper();
                backgroundThreadHandler = new Handler();
                Looper.loop();
            }
        });
        looperThread.start();
    }

    private void initCrashlyticsAndUncaughtThreadHandler() {
        UncaughtExceptionHandler myHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, final Throwable arg1) {
                arg1.printStackTrace();
                ArduinoConnectivityPopup.isOpened = false;
                moveTaskToBack(true);

                Enumeration<String> enumKey = ((OneSheeldApplication) getApplication()).getRunningShields().keys();
                while (enumKey.hasMoreElements()) {
                    String key = enumKey.nextElement();
                    ((OneSheeldApplication) getApplication())
                            .getRunningShields().get(key).resetThis();
                    ((OneSheeldApplication) getApplication())
                            .getRunningShields().remove(key);
                }
                if (((OneSheeldApplication) getApplication()).getAppFirmata() != null) {
                    while (!((OneSheeldApplication) getApplication())
                            .getAppFirmata().close())
                        ;
                }
                stopService();
                Intent in = new Intent(getIntent());
                PendingIntent intent = PendingIntent
                        .getActivity(getBaseContext(), 0, in,
                                getIntent().getFlags());

                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC,
                        System.currentTimeMillis() + 1000, intent);
                getThisApplication().setTutShownTimes(
                        getThisApplication().getTutShownTimes() + 1);
                killAllProcesses();
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        if (hasCrashlyticsApiKey(this)) {
            Crashlytics.start(this);
        }
    }

    Handler versionHandling = new Handler();
    FirmwareVersionQueryHandler versionChangingHandler = new FirmwareVersionQueryHandler() {
        ValidationPopup popub;

        @Override
        public void onVersionReceived(final int minorVersion,
                                      final int majorVersion) {
            versionHandling.post(new Runnable() {

                @Override
                public void run() {
                    Log.d("Onesheeld", minorVersion + "     " + majorVersion);
                    if (getThisApplication().getMinorVersion() != -1
                            && getThisApplication().getMajorVersion() != -1) {
                        if (majorVersion == getThisApplication()
                                .getMajorVersion()
                                && minorVersion != getThisApplication()
                                .getMinorVersion()) {
                            String msg = "";
                            try {
                                JSONObject obj = new JSONObject(
                                        ((OneSheeldApplication) getApplication())
                                                .getVersionWebResult());
                                try {
                                    msg += obj.getString("name") + "\n";
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                try {
                                    msg += obj.getString("description") + "\n";
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                try {
                                    msg += "Release Date: "
                                            + obj.getString("date");
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            popub = new ValidationPopup(
                                    MainActivity.this,
                                    "Optional Firmware Upgrade",
                                    msg,
                                    new ValidationPopup.ValidationAction("Now",
                                            new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    new FirmwareUpdatingPopup(
                                                            MainActivity.this/*
                                                                             * ,
																			 * false
																			 */)
                                                            .show();
                                                }
                                            }, true),
                                    new ValidationPopup.ValidationAction(
                                            "Not Now",
                                            new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    // TODO Auto-generated
                                                    // method
                                                    // stub

                                                }
                                            }, true));
                            if (!isFinishing())
                                popub.show();
                        } else if (majorVersion != getThisApplication()
                                .getMajorVersion()) {
                            String msg = "";
                            try {
                                JSONObject obj = new JSONObject(
                                        ((OneSheeldApplication) getApplication())
                                                .getVersionWebResult());
                                try {
                                    msg += obj.getString("name") + "\n";
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                try {
                                    msg += obj.getString("description") + "\n";
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                try {
                                    msg += "Release Date: "
                                            + obj.getString("date");
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            popub = new ValidationPopup(MainActivity.this,
                                    "Required Firmware Upgrade", msg,
                                    new ValidationPopup.ValidationAction(
                                            "Start",
                                            new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    FirmwareUpdatingPopup fup = new FirmwareUpdatingPopup(
                                                            MainActivity.this/*
                                                                             * ,
																			 * false
																			 */
                                                    );
                                                    fup.setCancelable(false);
                                                    fup.show();
                                                }
                                            }, true));
                            if (!isFinishing())
                                popub.show();
                        }
                    }
                }
            });
            getThisApplication().getTracker().send(
                    new HitBuilders.ScreenViewBuilder().setCustomDimension(1,
                            majorVersion + "." + minorVersion).build());
        }
    };
    ArduinoLibraryVersionChangeHandler arduinoLibraryVersionHandler = new ArduinoLibraryVersionChangeHandler() {
        ValidationPopup popub;

        @Override
        public void onArduinoLibraryVersionChange(final int version) {
            versionHandling.post(new Runnable() {

                @Override
                public void run() {
                    if (version < OneSheeldApplication.ARDUINO_LIBRARY_VERSION) {
                        popub = new ValidationPopup(
                                MainActivity.this,
                                "Arduino Library Update",
                                "There's a new version of 1Sheeld's Arduino library available on our website!",
                                new ValidationPopup.ValidationAction("OK",
                                        new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                popub.dismiss();
                                            }
                                        }, true));
                        if (!isFinishing())
                            popub.show();
                    }
                    getThisApplication().getTracker().send(
                            new HitBuilders.ScreenViewBuilder()
                                    .setCustomDimension(2, version + "")
                                    .build());
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    private BackOnconnectionLostHandler backOnConnectionLostHandler;

    public BackOnconnectionLostHandler getOnConnectionLostHandler() {
        if (backOnConnectionLostHandler == null) {
            backOnConnectionLostHandler = new BackOnconnectionLostHandler() {

                @Override
                public void handleMessage(Message msg) {
                    if (connectionLost) {
                        if (!ArduinoConnectivityPopup.isOpened
                                && !isFinishing())
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!ArduinoConnectivityPopup.isOpened
                                            && !isFinishing())
                                        new ArduinoConnectivityPopup(
                                                MainActivity.this).show();
                                }
                            });
                        if (getSupportFragmentManager()
                                .getBackStackEntryCount() > 1) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(0, 0, 0, 0)
                                    .commitAllowingStateLoss();
                            getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            getSupportFragmentManager()
                                    .executePendingTransactions();
                        }
                    }
                    connectionLost = false;
                    super.handleMessage(msg);
                }
            };
        }
        return backOnConnectionLostHandler;
    }

    public static class BackOnconnectionLostHandler extends Handler {
        public boolean canInvokeOnCloseConnection = true,
                connectionLost = false;
    }

    @Override
    public void onBackPressed() {
        resetSlidingMenu();
        MultiDirectionSlidingDrawer pinsView = (MultiDirectionSlidingDrawer) findViewById(R.id.pinsViewSlidingView);
        MultiDirectionSlidingDrawer settingsView = (MultiDirectionSlidingDrawer) findViewById(R.id.settingsSlidingView);
        if ((pinsView == null || (pinsView != null && !pinsView.isOpened()))
                && (settingsView == null || (settingsView != null && !settingsView
                .isOpened()))) {
            if (appSlidingMenu.isOpen()) {
                appSlidingMenu.closePane();
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    findViewById(R.id.getAvailableDevices).setOnClickListener(
                            new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                }
                            });
                    getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    moveTaskToBack(true);
                }
            }
        } else {
            if (pinsView.isOpened())
                pinsView.animateOpen();
            else if (settingsView.isOpened())
                settingsView.animateOpen();
        }
    }

    private void killAllProcesses() {
//        List<ApplicationInfo> packages;
//        PackageManager pm;
//        pm = getPackageManager();
//        //get a list of installed apps.
//        packages = pm.getInstalledApplications(0);
//
//        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//
//        for (ApplicationInfo packageInfo : packages) {
//            if (packageInfo.packageName.equals("com.integreight.onesheeld"))
//                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
//        }
        android.os.Process.killProcess(Process.myPid());
    }

    public void replaceCurrentFragment(int container, Fragment targetFragment,
                                       String tag, boolean addToBackStack, boolean animate) {
        // String backStateName = tag;
        // String fragmentTag = tag;
        if (!isFinishing()) {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(tag, 0);

            if (!fragmentPopped && manager.findFragmentByTag(tag) == null) {
                FragmentTransaction ft = manager.beginTransaction();
                if (animate)
                    ft.setCustomAnimations(R.anim.slide_out_right,
                            R.anim.slide_in_left, R.anim.slide_out_left,
                            R.anim.slide_in_right);
                ft.replace(container, targetFragment, tag);
                if (addToBackStack) {
                    ft.addToBackStack(tag);
                }
                ft.commit();
            }
        }
    }

    public void stopService() {
        this.stopService(new Intent(this, OneSheeldService.class));
    }

    public void finishManually() {
        isBackPressed = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        getThisApplication().getTracker().send(
                new HitBuilders.EventBuilder().setCategory("App lifecycle")
                        .setAction("Finished the app manually").build());
        ArduinoConnectivityPopup.isOpened = false;
        stopService();
        stopLooperThread();
        moveTaskToBack(true);
        if (((OneSheeldApplication) getApplication()).getAppFirmata() != null) {
            while (!((OneSheeldApplication) getApplication()).getAppFirmata()
                    .close())
                ;
        }
        // // unExpeted
        if (!isBackPressed) {
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
            // tryToSendNotificationsToAdmins(arg1);
            Enumeration<String> enumKey = ((OneSheeldApplication)
                    getApplication()).getRunningShields().keys();
            while (enumKey.hasMoreElements()) {
                String key = enumKey.nextElement();
                ((OneSheeldApplication) getApplication())
                        .getRunningShields().get(key).resetThis();
                ((OneSheeldApplication) getApplication())
                        .getRunningShields().remove(key);
            }
            Intent in = new Intent(getIntent());
            PendingIntent intent = PendingIntent.getActivity(
                    getBaseContext(), 0, in, getIntent().getFlags());

            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                    intent);
            killAllProcesses();
//                }
//            }).start();
        } else
            killAllProcesses();
        isBackPressed = false;
        super.onDestroy();
    }

    public void openMenu() {
        resetSlidingMenu();
        appSlidingMenu.openPane();
    }

    public void closeMenu() {
        resetSlidingMenu();
        appSlidingMenu.closePane();
    }

    public void enableMenu() {
        resetSlidingMenu();
        appSlidingMenu.setCanSlide(true);
    }

    public void disableMenu() {
        resetSlidingMenu();
        appSlidingMenu.closePane();
        appSlidingMenu.setCanSlide(false);
    }

    private void resetSlidingMenu() {
        if (appSlidingMenu == null) {
            appSlidingMenu = (AppSlidingLeftMenu) findViewById(R.id.sliding_pane_layout);
        }
    }

    public void setOnConnectToBluetooth(onConnectedToBluetooth listener) {
        this.onConnectToBlueTooth = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SheeldsList.REQUEST_CONNECT_DEVICE:
                break;
            case SheeldsList.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (onConnectToBlueTooth != null
                            && ArduinoConnectivityPopup.isOpened)
                        onConnectToBlueTooth.onConnect();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResumeFragments() {
        thisInstance = this;
        isForground = true;
        Crashlytics.setString("isBackground", "No");
        new Thread(new Runnable() {

            @Override
            public void run() {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appProcesses = activityManager
                        .getRunningAppProcesses();
                String apps = "";
                for (int i = 0; i < appProcesses.size(); i++) {
                    Log.d("Executed app", "Application executed : "
                            + appProcesses.get(i).processName + "\t\t ID: "
                            + appProcesses.get(i).pid + "");
                    apps += appProcesses.get(i).processName + "\n";
                }
                Crashlytics.setString("Running apps", apps);
            }
        }).start();

        super.onResumeFragments();
        NfcOnResumeFragments();
    }

    private void NfcOnResumeFragments() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            //NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(thisInstance);
            //if (nfcAdapter != null)
                if (((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())) != null) {
                    PackageManager packageManager = getPackageManager();
                    packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld","com.integreight.onesheeld.NFCUtils-alias"),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,BIND_NOT_FOREGROUND);
                    ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).setupForegroundDispatch(thisInstance);
                }
        }
    }

    long pausingTime = 0;

    @Override
    protected void onPause() {
        isForground = false;
        pausingTime = System.currentTimeMillis();
        float hours = TimeUnit.MILLISECONDS.toSeconds(System
                .currentTimeMillis() - pausingTime);
        float minutes = TimeUnit.MILLISECONDS.toMinutes(System
                .currentTimeMillis() - pausingTime);
        float seconds = TimeUnit.MILLISECONDS.toHours(System
                .currentTimeMillis() - pausingTime);
        Crashlytics.setString("isBackground", "since " + hours + " hours - "
                + minutes + " minutes - " + seconds + " seocnds");
        new Thread(new Runnable() {

            @Override
            public void run() {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appProcesses = activityManager
                        .getRunningAppProcesses();
                String apps = "";
                for (int i = 0; i < appProcesses.size(); i++) {
                    Log.d("Executed app", "Application executed : "
                            + appProcesses.get(i).processName + "\t\t ID: "
                            + appProcesses.get(i).pid + "");
                    apps += appProcesses.get(i).processName + "  ||||||  ";
                }
                Crashlytics.setString("Running apps", apps);
            }
        }).start();
        NfcOnPause();
        super.onPause();
    }

    private void NfcOnPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            //NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(thisInstance);
            //if (nfcAdapter != null)
                //if (nfcAdapter.isEnabled())
                    if (((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())) != null) {
                        PackageManager packageManager = getPackageManager();
                        packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld","com.integreight.onesheeld.NFCUtils-alias"),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,BIND_NOT_FOREGROUND);
                        ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).stopForegroundDispatch(thisInstance);
                    }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        getNfcIntent(intent);
        super.onNewIntent(intent);
    }

    private void getNfcIntent(Intent intent) {
        String action = intent.getAction();
        if(action != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                //NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(thisInstance);
                //if (nfcAdapter != null)
                //if (nfcAdapter.isEnabled())
                if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))
                    if (((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())) != null) {
                        ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).handleIntent(intent);
                    }
            }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);
        }
    }

    public static boolean hasCrashlyticsApiKey(Context context) {

        boolean hasValidKey = false;
        try {

            Context appContext = context.getApplicationContext();
            ApplicationInfo ai = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(),
                    PackageManager.GET_META_DATA);

            Bundle bundle = ai.metaData;
            if (bundle != null) {

                String apiKey = bundle.getString("com.crashlytics.ApiKey");
                hasValidKey = apiKey != null && !apiKey.equals("0000000000000000000000000000000000000000");

            }

        } catch (PackageManager.NameNotFoundException e) {

            // Should not happen since the name was determined dynamically from the app context.
//            Log.e(LOGTAG, "Unexpected NameNotFound.", e);

        }

        return hasValidKey;

    }

}
