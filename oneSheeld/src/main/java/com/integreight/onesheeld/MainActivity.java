package com.integreight.onesheeld;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.appFragments.ShieldsOperations;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup.onConnectedToBluetooth;
import com.integreight.onesheeld.popup.FirmwareUpdatingPopup;
import com.integreight.onesheeld.popup.ValidationPopup;
import com.integreight.onesheeld.sdk.FirmwareVersion;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.sdk.OneSheeldVersionQueryCallback;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class MainActivity extends FragmentActivity {
    public static final int PREMISSION_REQUEST_CODE = 1;
    public static final int DRAW_OVER_APPS_REQUEST_CODE = 2;
    public static final String IS_CONTEXT_MENU_BUTTON_TUTORIAL_SHOWN_SP = "com.integreight.onesheeld.IS_CONTEXT_MENU_BUTTON_TUTORIAL_SHOWN_SP";
    public static String currentShieldTag = null;
    public static MainActivity thisInstance;
    public AppSlidingLeftMenu appSlidingMenu;
    public boolean isForground = false;
    private boolean isBackPressed = false;
    TextView oneSheeldLogo;

    private CopyOnWriteArrayList<OnSlidingMenueChangeListner> onChangeSlidingLockListeners = new CopyOnWriteArrayList<>();

    public OneSheeldApplication getThisApplication() {
        return (OneSheeldApplication) getApplication();
    }

    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        handleNotificationWithUrlIntent(getIntent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#CC3a3a3a"));
        }
        setContentView(R.layout.one_sheeld_main);
        oneSheeldLogo = (TextView) findViewById(R.id.currentViewTitle);
        initLooperThread();
        if (savedInstance == null || getThisApplication().isConnectedToBluetooth()) {
//            if (savedInstance != null) {
//                int count = getSupportFragmentManager().getBackStackEntryCount();
//                while (count > 0) {
//                    getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().getFragments().get(count)).commit();
//                    count --;
//                }
//            }
            replaceCurrentFragment(R.id.appTransitionsContainer,
                    SheeldsList.getInstance(), "base", true, false);
        }
        postConfigChange();
        resetSlidingMenu();
        thisInstance = this;
        if (getThisApplication().getShowTutAgain()
                && getThisApplication().getTutShownTimes() < 6)
            startActivity(new Intent(MainActivity.this, Tutorial.class));
        AppRate.with(this)
                .setInstallDays(7)
                .setLaunchTimes(5)
                .setRemindInterval(2)
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Map<String, String> hit = null;
                        switch (which) {
                            case Dialog.BUTTON_NEGATIVE:
                                hit = new HitBuilders.EventBuilder()
                                        .setCategory("App Rating Dialog")
                                        .setAction("No")
                                        .build();
                                break;
                            case Dialog.BUTTON_NEUTRAL:
                                hit = new HitBuilders.EventBuilder()
                                        .setCategory("App Rating Dialog")
                                        .setAction("Later")
                                        .build();
                                break;
                            case Dialog.BUTTON_POSITIVE:
                                hit = new HitBuilders.EventBuilder()
                                        .setCategory("App Rating Dialog")
                                        .setAction("Yes")
                                        .build();
                                break;
                        }
                        if (hit != null) getThisApplication()
                                .getTracker()
                                .send(hit);
                    }
                })
                .monitor();
    }

    public Thread looperThread;
    public Handler backgroundThreadHandler;
    Handler versionHandling = new Handler();
    public OneSheeldVersionQueryCallback versionQueryCallback = new OneSheeldVersionQueryCallback() {
        ValidationPopup popub;

        @Override
        public void onFirmwareVersionQueryResponse(OneSheeldDevice device, FirmwareVersion firmwareVersion) {
            super.onFirmwareVersionQueryResponse(device, firmwareVersion);
            final int minorVersion = firmwareVersion.getMinorVersion();
            final int majorVersion = firmwareVersion.getMajorVersion();
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
                                    msg += obj.getString("date");
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            popub = new ValidationPopup(
                                    MainActivity.this,
                                    getString(R.string.firmware_upgrade_decision_dialog_optional_firmware_upgrade),
                                    msg,
                                    new ValidationPopup.ValidationAction(getString(R.string.firmware_upgrade_decision_dialog_now_button),
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
                                            getString(R.string.firmware_upgrade_decision_dialog_not_now_button),
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
                                    msg += obj.getString("date");
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            popub = new ValidationPopup(MainActivity.this,
                                    getString(R.string.firmware_upgrade_decision_dialog_required_firmware_upgrade), msg,
                                    new ValidationPopup.ValidationAction(
                                            getString(R.string.firmware_upgrade_decision_dialog_start),
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

        @Override
        public void onLibraryVersionQueryResponse(OneSheeldDevice device, final int libraryVersion) {
            super.onLibraryVersionQueryResponse(device, libraryVersion);
            versionHandling.post(new Runnable() {

                @Override
                public void run() {
                    if (libraryVersion < OneSheeldApplication.ARDUINO_LIBRARY_VERSION) {
                        popub = new ValidationPopup(
                                MainActivity.this,
                                getString(R.string.library_upgrade_dialog_arduino_library_update),
                                getString(R.string.library_upgrade_dialog_theres_a_new_version_of_1sheelds_arduino_library_available_on_our_website),
                                new ValidationPopup.ValidationAction(getString(R.string.library_upgrade_dialog_ok_button),
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
                                    .setCustomDimension(2, libraryVersion + "")
                                    .build());
                }
            });
        }
    };
    boolean isConfigChanged = false;
    long pausingTime = 0;
    private onConnectedToBluetooth onConnectToBlueTooth = null;
    private Looper backgroundHandlerLooper;
    private BackOnconnectionLostHandler backOnConnectionLostHandler;

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

    @Override
    protected void onResume() {
        super.onResume();
        thisInstance = this;
    }

    public BackOnconnectionLostHandler getOnConnectionLostHandler() {
        if (backOnConnectionLostHandler == null) {
            backOnConnectionLostHandler = new BackOnconnectionLostHandler(this);
        }
        return backOnConnectionLostHandler;
    }

    @Override
    public void onBackPressed() {
        Log.d("Test", "Back Pressed");
        ///// Camera Preview
        if (getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name()) != null)
            try {
                ((CameraShield) getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name())).hidePreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        if (getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name()) != null)
            try {
                ((ColorDetectionShield) getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name())).hidePreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        if (getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name()) != null)
            try {
                ((FaceDetectionShield) getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name())).hidePreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
                    if (findViewById(R.id.isMenuOpening) != null)
                        ((CheckBox) findViewById(R.id.isMenuOpening))
                                .setChecked(false);
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
        Process.killProcess(Process.myPid());
    }

    public void replaceCurrentFragment(int container, Fragment targetFragment,
                                       String tag, boolean addToBackStack, boolean animate) {
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
        destroyIt();
    }

    private void preConfigChange() {
        Enumeration<String> enumKey = ((OneSheeldApplication)
                getApplication()).getRunningShields().keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            ((OneSheeldApplication) getApplication())
                    .getRunningShields().get(key).preConfigChangeThis();
        }
    }

    private void postConfigChange() {
        Enumeration<String> enumKey = ((OneSheeldApplication)
                getApplication()).getRunningShields().keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            ((OneSheeldApplication) getApplication())
                    .getRunningShields().get(key).updateActivty(this);
            ((OneSheeldApplication) getApplication())
                    .getRunningShields().get(key).postConfigChangeThis();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isConfigChanged = true;
        preConfigChange();
        super.onConfigurationChanged(newConfig);
        postConfigChange();
    }

    private void destroyIt() {
        if (!isConfigChanged) {
            getThisApplication().getTracker().send(
                    new HitBuilders.EventBuilder().setCategory("App lifecycle")
                            .setAction("Finished the app manually").build());
            ArduinoConnectivityPopup.isOpened = false;
            stopService();
            stopLooperThread();
            moveTaskToBack(true);
            OneSheeldSdk.getManager().disconnectAll();
            // // unExpeted
            if (!isBackPressed) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 100,
                            intent);
                } else {
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                            intent);
                }
                killAllProcesses();
            } else
                killAllProcesses();
        }
        isConfigChanged = false;
        isBackPressed = false;
    }

    @Override
    protected void onDestroy() {
        ArduinoConnectivityPopup.isOpened = false;
        super.onDestroy();
    }

    public void openMenu() {
        resetSlidingMenu();
        appSlidingMenu.openPane();
    }

    public boolean isMenuOpened() {
        resetSlidingMenu();
        return appSlidingMenu.isOpen();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                appSlidingMenu.setFitsSystemWindows(true);
            appSlidingMenu.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {

                }

                @Override
                public void onPanelOpened(View panel) {

                }

                @Override
                public void onPanelClosed(View panel) {
                    if (onChangeSlidingLockListeners != null && onChangeSlidingLockListeners.size() > 0) {
                        for (OnSlidingMenueChangeListner onChangeListener : onChangeSlidingLockListeners) {
                            onChangeListener.onMenuClosed();
                        }
                    }
                }
            });
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
                    Toast.makeText(this, R.string.general_toasts_bluetooth_was_not_enabled_toast,
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        if (onConnectToBlueTooth != null
                                && ArduinoConnectivityPopup.isOpened && !((OneSheeldApplication) getApplication()).getIsDemoMode())
                            onConnectToBlueTooth.onConnect();
                    } else {
                        checkAndAskForLocationPermission();
                    }
                }
                break;
            case DRAW_OVER_APPS_REQUEST_CODE:
                if (canDrawOverApps()) {
                    showToast(getString(R.string.main_activity_draw_over_apps_enabled_you_can_select_the_shield));
                } else {
                    showToast(getString(R.string.main_activity_draw_over_apps_was_not_enabled));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Boolean checkForLocationPermission() {
        return (ContextCompat.checkSelfPermission(thisInstance,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    public void checkAndAskForLocationPermission() {
        if (ContextCompat.checkSelfPermission(thisInstance,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisInstance,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder locationPremissionExplanationDialog = new AlertDialog.Builder(thisInstance);
                locationPremissionExplanationDialog.setMessage(R.string.main_activity_bluetooth_scan_needs_location_permission).setPositiveButton(R.string.main_activity_allow_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisInstance,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                MainActivity.PREMISSION_REQUEST_CODE);
                    }
                }).setNegativeButton(R.string.main_activity_deny_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast(getString(R.string.main_activity_location_permission_denied));
                    }
                }).create();
                locationPremissionExplanationDialog.show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(thisInstance,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.LOCATION_HARDWARE},
                        MainActivity.PREMISSION_REQUEST_CODE);
            }
        } else {
            if (onConnectToBlueTooth != null
                    && ArduinoConnectivityPopup.isOpened)
                onConnectToBlueTooth.onConnect();
        }
    }

    @Override
    protected void onResumeFragments() {
        thisInstance = this;
        isForground = true;
        CrashlyticsUtils.setString("isBackground", "No");
        new Thread(new Runnable() {

            @Override
            public void run() {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appProcesses = activityManager
                        .getRunningAppProcesses();
                if(appProcesses!=null) {
                    String apps = "";
                    for (int i = 0; i < appProcesses.size(); i++) {
                        Log.d("Executed app", "Application executed : "
                                + appProcesses.get(i).processName + "\t\t ID: "
                                + appProcesses.get(i).pid + "");
                        apps += appProcesses.get(i).processName + "\n";
                    }
                    CrashlyticsUtils.setString("Running apps", apps);
                }
            }
        }).start();

        super.onResumeFragments();
        resumeNfcMainActivityFragments();
    }

    private void resumeNfcMainActivityFragments() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld", "com.integreight.onesheeld.NFCUtils-alias"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).setupForegroundDispatch();
            }
        }
    }

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
        CrashlyticsUtils.setString("isBackground", "since " + hours + " hours - "
                + minutes + " minutes - " + seconds + " seconds");
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
                CrashlyticsUtils.setString("Running apps", apps);
            }
        }).start();
        pauseMainActivityNfc();
        super.onPause();
    }

    private void pauseMainActivityNfc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld", "com.integreight.onesheeld.NFCUtils-alias"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).stopForegroundDispatch();
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
    protected void onNewIntent(final Intent intent) {
        if (intent!=null && intent.getStringExtra("url")!=null && intent.getStringExtra("url").length()>0){
            handleNotificationWithUrlIntent(intent);
        }
        else if (getThisApplication().getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
            if (findViewById(R.id.progressShieldInit) != null && getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName()) == null) {
                findViewById(R.id.progressShieldInit)
                        .setVisibility(View.VISIBLE);
                findViewById(R.id.operationsLogo)
                        .setVisibility(View.INVISIBLE);
                findViewById(R.id.operationsLogo).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressShieldInit)
                                .setVisibility(View.INVISIBLE);
                        findViewById(R.id.operationsLogo)
                                .setVisibility(View.VISIBLE);
                    }
                }, 1000);
            }
        }
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                getNfcIntent(intent);
            }
        });
        super.onNewIntent(intent);
    }

    private void getNfcIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                        if (getThisApplication().getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
                            ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).handleIntent(intent);
                        }
                    }
                }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);
        }
    }

    public void registerSlidingMenuListner(OnSlidingMenueChangeListner listner) {
        if (!onChangeSlidingLockListeners.contains(listner))
            onChangeSlidingLockListeners.add(listner);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PREMISSION_REQUEST_CODE) {
            if (permissions.length > 0) {
                switch (permissions[0]) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (grantResults.length > 0
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            if (onConnectToBlueTooth != null
                                    && ArduinoConnectivityPopup.isOpened)
                                onConnectToBlueTooth.onConnect();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                                    showToast(getString(R.string.main_activity_location_permission_denied));
                                else
                                    showToast(getString(R.string.main_activity_please_turn_on_location_permission));
                            }
                        }
                        break;
                    default:
                        Boolean isEnabled = true;
                        for (int permissionsCount = 0; permissionsCount < grantResults.length; permissionsCount++) {
                            if (grantResults[permissionsCount] != PackageManager.PERMISSION_GRANTED)
                                isEnabled = false;
                        }
                        if (isEnabled) {
                            showToast(getString(R.string.general_toasts_you_can_select_the_shield_now_toast));
                            // permission was granted, yay! Do the
                            // contacts-related task you need to do.
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Boolean isShouldShowRequestPermissionRationale = true;
                                for (int permissionsCount = 0; permissionsCount < permissions.length; permissionsCount++) {
                                    if (shouldShowRequestPermissionRationale(permissions[permissionsCount]) && grantResults[permissionsCount] != PackageManager.PERMISSION_GRANTED)
                                        isShouldShowRequestPermissionRationale = false;
                                }
                                if (!isShouldShowRequestPermissionRationale)
                                    showToast(getString(R.string.main_activity_this_shield_needs_some_permissions));
                                else
                                    showToast(getString(R.string.main_activity_your_device_didnt_allow_this_shield_permissions));
                            } else {
                                showToast(getString(R.string.main_activity_this_shield_needs_some_permissions));
                            }
                        }
                        break;
                }
            }
            return;
        }
    }

    public void showMenuButtonTutorialOnce() {
        if (Build.VERSION.SDK_INT >= 11 && oneSheeldLogo != null) {
            ViewTarget target = new ViewTarget(oneSheeldLogo);
            if (!getThisApplication().getAppPreferences().getBoolean(IS_CONTEXT_MENU_BUTTON_TUTORIAL_SHOWN_SP, false)) {
                new ShowcaseView.Builder(this)
                        .setTarget(target)
                        .withMaterialShowcase()
                        .setContentTitle(getString(R.string.context_menu_tutorial_open_context_menu))
                        .setContentText(getString(R.string.context_menu_tutorial_upgrade_the_firmware_clear_the_automatic_connection_and_see_the_tutorial_again_after_opening_the_context_menu_by_clicking_on_1sheeld_logo))
                        .setStyle(R.style.CustomShowcaseTheme)
                        .hideOnTouchOutside()
                        .build().hideButton();
                getThisApplication().getAppPreferences().edit().putBoolean(IS_CONTEXT_MENU_BUTTON_TUTORIAL_SHOWN_SP, true).commit();
            }
        }
    }

    public boolean canDrawOverApps() {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    public void requestDrawOverApps() {
        if (!canDrawOverApps()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_APPS_REQUEST_CODE);
        }
    }

    public interface OnSlidingMenueChangeListner {
        public void onMenuClosed();
    }

    private void handleNotificationWithUrlIntent(Intent intent) {
        if (intent != null && intent.getStringExtra("url") != null && intent.getStringExtra("url").length() > 0) {
            String url = intent.getStringExtra("url");
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(notificationIntent);
//            finish();
        }
    }

    public class BackOnconnectionLostHandler extends Handler {
        public boolean canInvokeOnCloseConnection = true,
                connectionLost = false;

        private final WeakReference<MainActivity> mTarget;

        public BackOnconnectionLostHandler(MainActivity activity) {
            this.mTarget = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mTarget.get();
            if (activity != null) {
                if (!((OneSheeldApplication) activity.getApplication()).getIsDemoMode() && !getThisApplication().isConnectedToBluetooth()) {
                    if (connectionLost) {
                        if (!ArduinoConnectivityPopup.isOpened
                                && !activity.isFinishing())
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!ArduinoConnectivityPopup.isOpened
                                            && !activity.isFinishing()) {
                                        new ArduinoConnectivityPopup(
                                                activity).show();
                                    }
                                }
                            });
                        if (activity.getSupportFragmentManager()
                                .getBackStackEntryCount() > 1) {
                            activity.getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(0, 0, 0, 0)
                                    .commitAllowingStateLoss();
                            activity.getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            activity.getSupportFragmentManager()
                                    .executePendingTransactions();
                        }
                    }
                    connectionLost = false;
                }
            }
            super.handleMessage(msg);
        }
    }
}
