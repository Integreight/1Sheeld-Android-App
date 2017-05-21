package com.integreight.onesheeld.appFragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.onesheeld.BuildConfig;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.Tutorial;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.Shield;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.popup.FirmwareUpdatingPopup;
import com.integreight.onesheeld.popup.ValidationPopup;
import com.integreight.onesheeld.popup.ValidationPopup.ValidationAction;
import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield;
import com.integreight.onesheeld.shields.controller.TaskerShield;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.OneSheeldEditText;
import com.manuelpeinado.quickreturnheader.QuickReturnHeaderHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import hotchemi.android.rate.AppRate;

public class SheeldsList extends Fragment {
    View v;
    boolean isInflated = false;
    private ListView mListView;
    private static SheeldsList thisInstance;
    private ShieldsListAdapter adapter;
    OneSheeldEditText searchBox;
    private static final String TAG = "ShieldsList";
    MainActivity activity;
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 3;


    public static SheeldsList getInstance() {
        if (thisInstance == null) {
            thisInstance = new SheeldsList();
        }
        return thisInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isInflated = (v == null);
        if (v == null) {
            QuickReturnHeaderHelper helper = new QuickReturnHeaderHelper(
                    activity, R.layout.app_sheelds_list,
                    R.layout.shields_list_search_area);
            v = helper.createView();
            mListView = (ListView) v.findViewById(android.R.id.list);
        } else {
            try {
                ((ViewGroup) v.getParent()).removeView(v);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return v;
    }

    @Override
    public void onStop() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (activity != null && searchBox != null) {
                    InputMethodManager imm = (InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                }
            }
        });
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = (MainActivity) getActivity();
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        MainActivity.currentShieldTag = null;
        this.activity = (MainActivity) getActivity();
        if (adapter != null)
            adapter.setActivity(activity);
        activity.disableMenu();
        activity.hideSoftKeyboard();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (activity != null
                        && activity.getSupportFragmentManager() != null) {
                    activity.findViewById(R.id.currentViewTitle).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.openOptionsMenu();
                        }
                    });
                    activity.findViewById(R.id.getAvailableDevices)
                            .setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (getActivity().findViewById(R.id.progressShieldInit).getVisibility() != View.VISIBLE) {
                                        activity.findViewById(R.id.cancelConnection)
                                                .setOnClickListener(
                                                        new View.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    View v) {
                                                                // TODO
                                                                // Auto-generated
                                                                // method stub

                                                            }
                                                        });
                                        activity.findViewById(R.id.currentViewTitle).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // TODO
                                                // 1Sheeld Logo In Header
                                            }
                                        });
                                        launchShieldsOperationActivity();
                                    }
                                }
                            });
                    List<Fragment> frags = activity.getSupportFragmentManager()
                            .getFragments();
                    for (Fragment frag : frags) {
                        if (frag != null
                                && !frag.getClass().getName()
                                .equals(SheeldsList.class.getName())) {
                            if (activity != null
                                    && activity != null
                                    && activity.getSupportFragmentManager() != null) {
                                FragmentTransaction ft = activity
                                        .getSupportFragmentManager()
                                        .beginTransaction();
                                frag.onDestroy();
                                ft.remove(frag);
                                ft.commitAllowingStateLoss();
                            }
                        }
                    }
                }
            }
        }, 500);
        ((ViewGroup) activity.findViewById(R.id.getAvailableDevices))
                .getChildAt(1).setBackgroundResource(
                R.drawable.shields_list_shields_operation_button);
        if (getApplication().getIsDemoMode() && !getApplication().isConnectedToBluetooth())
            ((ViewGroup) activity.findViewById(R.id.cancelConnection)).getChildAt(1).setBackgroundResource(R.drawable.scan_button);
        else
            ((ViewGroup) activity.findViewById(R.id.cancelConnection)).getChildAt(1).setBackgroundResource(R.drawable.bluetooth_disconnect_button);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (activity != null
                        && activity.findViewById(R.id.cancelConnection) != null)
                    activity.findViewById(R.id.cancelConnection)
                            .setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (!getApplication().getIsDemoMode() ||
                                            getApplication().isConnectedToBluetooth()) {
                                        if (activity.getSupportFragmentManager()
                                                .getBackStackEntryCount() > 1) {
                                            activity.getSupportFragmentManager()
                                                    .popBackStack();
                                            activity.getSupportFragmentManager()
                                                    .executePendingTransactions();
                                        }
                                        OneSheeldSdk.getManager().disconnectAll();
                                    } else {
                                        Log.test("Test", "Cannot disconnect in demoMode");
                                        getApplication().setIsDemoMode(false);
                                    }
                                    if (!ArduinoConnectivityPopup.isOpened) {
                                        ArduinoConnectivityPopup.isOpened = true;
                                        new ArduinoConnectivityPopup(activity)
                                                .show();
                                    }
                                }
                            });
            }
        }, 500);
        activity.getOnConnectionLostHandler().canInvokeOnCloseConnection = true;
//        getApplication()
//                .setArduinoFirmataEventHandler(sheeldsFirmataHandler);
        OneSheeldSdk.getManager().addConnectionCallback(connectionCallback);
        OneSheeldSdk.getManager().addErrorCallback(errorCallback);
        if (!getApplication().isConnectedToBluetooth()) {
            if (!ArduinoConnectivityPopup.isOpened && !getApplication().getIsDemoMode()) {
                new ArduinoConnectivityPopup(activity).show();
            }
        }
        CrashlyticsUtils.setString("Current View", "Shields List");
        getApplication().getTracker()
                .setScreenName("Main Shields List");
        getApplication().getTracker().send(
                new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
    }

    private OneSheeldApplication getApplication() {
        return (OneSheeldApplication) activity.getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        this.activity = (MainActivity) getActivity();
        if (adapter != null)
            adapter.setActivity(activity);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // setRetainInstance(true);
        this.activity = (MainActivity) getActivity();
        if (adapter != null)
            adapter.setActivity(activity);
        if (isInflated)
            initView();
        super.onActivityCreated(savedInstanceState);
    }


    private void initView() {
        adapter = new ShieldsListAdapter(activity);
        mListView.setAdapter(adapter);
        mListView.setSelection(1);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setDrawingCacheEnabled(true);
        searchBox = (OneSheeldEditText) v.findViewById(R.id.searchArea);
        searchBox.setAdapter(adapter);
        searchBox.setDropDownHeight(0);
        v.findViewById(R.id.remember).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        ((OneSheeldApplication) getActivity().getApplication()).setRememberedShields(AppShields.getInstance().getSelectedShields());
                    }
                });
        v.findViewById(R.id.deselect_all).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        InputMethodManager imm = (InputMethodManager) activity
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
                                0);
                        for (int i = 0; i < AppShields.getInstance()
                                .getShieldsArray().size(); i++) {
                            AppShields.getInstance().getShield(i).mainActivitySelection = false;
                        }
                        searchBox.setText("");
                        adapter.reset();
                    }
                });
        v.findViewById(R.id.clearBox).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        InputMethodManager imm = (InputMethodManager) activity
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
                                0);
                        searchBox.setText("");
                    }
                });
        mListView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                return false;
            }
        });
    }

    private void launchShieldsOperationActivity() {
        if (getActivity().findViewById(R.id.progressShieldInit).getVisibility() != View.VISIBLE) {
            if (!isAnyShieldsSelected()) {
                Toast.makeText(activity, R.string.shields_list_select_at_least_1_shield_toast,
                        Toast.LENGTH_LONG).show();
                activity.findViewById(R.id.currentViewTitle).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.openOptionsMenu();
                    }
                });
                activity.findViewById(R.id.cancelConnection)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (!getApplication().getIsDemoMode() ||
                                        getApplication().isConnectedToBluetooth()) {
                                    if (activity.getSupportFragmentManager()
                                            .getBackStackEntryCount() > 1) {
                                        activity.getSupportFragmentManager()
                                                .popBackStack();
                                        activity.getSupportFragmentManager()
                                                .executePendingTransactions();
                                    }
                                    OneSheeldSdk.getManager().disconnectAll();
                                } else {
                                    Log.test("Test", "Cannot disconnect in demoMode");
                                    getApplication().setIsDemoMode(false);
                                }
                                if (!ArduinoConnectivityPopup.isOpened) {
                                    ArduinoConnectivityPopup.isOpened = true;
                                    new ArduinoConnectivityPopup(activity)
                                            .show();
                                }
                            }
                        });
                return;
            } else {
                activity.replaceCurrentFragment(R.id.appTransitionsContainer,
                        ShieldsOperations.getInstance(),
                        ShieldsOperations.class.getName(), true, true);
                activity.findViewById(R.id.getAvailableDevices)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub

                            }
                        });
            }
        }
    }

    OneSheeldErrorCallback errorCallback = new OneSheeldErrorCallback() {
        @Override
        public void onError(OneSheeldDevice device, OneSheeldError error) {
            super.onError(device, error);
            if (activity != null
                    && activity.getThisApplication().taskerController != null) {
                activity.getThisApplication().taskerController.reset();
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getApplication()
                            .endConnectionTimerAndReport();
                    UIShield.setConnected(false);
                    adapter.notifyDataSetChanged();
                    if (activity.getSupportFragmentManager().getBackStackEntryCount() > 1) {
                        activity.getSupportFragmentManager().popBackStack();
                        activity.getSupportFragmentManager()
                                .executePendingTransactions();
                    }
                    if (!ArduinoConnectivityPopup.isOpened && !getApplication().getIsDemoMode()) {
                        new ArduinoConnectivityPopup(activity).show();
                    }
                }
            });
        }
    };
    OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
        @Override
        public void onConnect(OneSheeldDevice device) {
            super.onConnect(device);
            ((OneSheeldApplication) activity.getApplication()).setConnectedDevice(device);
            if (activity.getThisApplication().getConnectedDevice() != null)
                activity.getThisApplication().getConnectedDevice().addVersionQueryCallback(activity.versionQueryCallback);
            Intent intent = new Intent(activity, OneSheeldService.class);
            intent.putExtra(ArduinoConnectivityPopup.EXTRA_DEVICE_NAME, device.getName());
            activity.startService(intent);
            getApplication().setConnectedDevice(device);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    activity.getThisApplication().taskerController = new TaskerShield(
                            activity, UIShield.TASKER_SHIELD.name());
                    Log.e(TAG, "- ARDUINO CONNECTED -");
                    getApplication().getTracker()
                            .send(new HitBuilders.ScreenViewBuilder().setNewSession()
                                    .build());
                    getApplication()
                            .startConnectionTimer();
//                    if (isOneSheeldServiceRunning()) {
                    if (adapter != null)
                        adapter.applyToControllerTable();
//                    }
                    AppRate.showRateDialogIfMeetsConditions(activity);
                    activity.showMenuButtonTutorialOnce();

                }
            });
        }

        @Override
        public void onDisconnect(OneSheeldDevice device) {
            super.onDisconnect(device);
            getApplication().setConnectedDevice(null);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (activity != null) {
                        if (activity.getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name()) != null)
                            try {
                                ((CameraShield) activity.getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name())).hidePreview();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        if (activity.getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name()) != null)
                            try {
                                ((ColorDetectionShield) activity.getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name())).hidePreview();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        if (activity.getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name()) != null)
                            try {
                                ((FaceDetectionShield) activity.getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name())).hidePreview();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        if (activity.getThisApplication().taskerController != null) {
                            activity.getThisApplication().taskerController.reset();
                        }
                        getApplication()
                                .endConnectionTimerAndReport();
                        activity.getOnConnectionLostHandler().connectionLost = true;
                        if (activity.getOnConnectionLostHandler().canInvokeOnCloseConnection
                                || activity.isForground)
                            activity.getOnConnectionLostHandler().sendEmptyMessage(0);
                        else {
                            List<Fragment> frags = activity.getSupportFragmentManager()
                                    .getFragments();
                            for (Fragment frag : frags) {
                                if (frag != null
                                        && !frag.getClass().getName()
                                        .equals(SheeldsList.class.getName())
                                        && !frag.getClass()
                                        .getName()
                                        .equals(ShieldsOperations.class
                                                .getName())) {
                                    FragmentTransaction ft = activity
                                            .getSupportFragmentManager()
                                            .beginTransaction();
                                    ft.setCustomAnimations(0, 0, 0, 0);
                                    frag.onDestroy();
                                    ft.remove(frag);
                                    ft.commitAllowingStateLoss();
                                }
                            }
                        }
                        Enumeration<String> enumKey = ((OneSheeldApplication) activity
                                .getApplication()).getRunningShields().keys();
                        while (enumKey.hasMoreElements()) {
                            String key = enumKey.nextElement();
                            getApplication()
                                    .getRunningShields().get(key).resetThis();
                            getApplication()
                                    .getRunningShields().remove(key);
                        }
                        activity.stopService();
                    }

                }
            });
        }

        @Override
        public void onConnectionRetry(OneSheeldDevice device, int retryCount) {
            super.onConnectionRetry(device, retryCount);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflate.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_bootloader_popup:
                if (getApplication().isConnectedToBluetooth())
                    new FirmwareUpdatingPopup(activity/* , false */)
                            .show();
                else
                    activity.showToast(activity.getString(R.string.shields_list_please_connect_to_your_board_first));
                return true;
            case R.id.action_settings:
                getApplication()
                        .setLastConnectedDevice(null);
                return true;
            case R.id.appTutorial:
                activity.startActivity(new Intent(activity, Tutorial.class)
                        .putExtra("isMenu", true));
                return true;
            case R.id.aboutDialogButton:
                showAboutDialog();
                return true;
        }

        return false;
    }

    private void showAboutDialog() {
        // TODO Auto-generated method stub
        String stringDate = null;
        try {
            stringDate = DateFormat.format("dd-MM-yyyy hh:mm:ss", new Date(BuildConfig.TIMESTAMP)).toString();
            String versionName = BuildConfig.VERSION_NAME;
            int versionCode = BuildConfig.VERSION_CODE;
            String installationIdString = "";
            ValidationAction shareConnectionId = null;
            String firmwareVersion = "";
            if (getApplication().isConnectedToBluetooth()
                    && getApplication().getConnectedDevice().getFirmwareVersion().getMajorVersion() != 0) {
                firmwareVersion = "\n" + activity.getString(R.string.about_dialog_firmware_version) + ": v"
                        + getApplication().getConnectedDevice().getFirmwareVersion().getMajorVersion()
                        + "."
                        + getApplication().getConnectedDevice().getFirmwareVersion().getMinorVersion() + "\n\n";
            }
            final ValidationPopup popup = new ValidationPopup(
                    activity,
                    activity.getString(R.string.about_dialog_about_1sheeld),
                    activity.getString(R.string.about_dialog_developed_with_love_by_integreight_inc_team_in_cairo_egypt) + "\n\n"
                            + activity.getString(R.string.about_dialog_if_you_have_any_question_please_visit_our_website_or_drop_us_an_email_on_info_integreight_com) + "\n\n"
                            + activity.getString(R.string.about_dialog_app_version) + ": "
                            + versionName
                            + " ("
                            + versionCode
                            + ")"
                            + firmwareVersion
                            + (stringDate != null ? "\n" + activity.getString(R.string.about_dialog_app_was_last_updated_on) + " "
                            + stringDate
                            : "")
                            + "\n\n"
                            + activity.getString(R.string.about_dialog_if_you_are_interested_in_this_apps_source_code_please_visit_our_github_page_github_com_integreight) + "\n\n"
                            + installationIdString);
            ValidationAction ok = new ValidationPopup.ValidationAction(activity.getString(R.string.about_dialog_okay),
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            popup.dismiss();
                        }
                    }, true);

            popup.addValidationAction(ok);
            if (shareConnectionId != null)
                popup.addValidationAction(shareConnectionId);
            if (!activity.isFinishing())
                popup.show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isOneSheeldServiceRunning() {
        if (activity != null) {
            ActivityManager manager = (ActivityManager) activity
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager
                    .getRunningServices(Integer.MAX_VALUE)) {
                if (OneSheeldService.class.getName().equals(
                        service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAnyShieldsSelected() {
        for (int j = 0; j < AppShields.getInstance().getShieldsArray().size(); j++) {
            Shield shield = AppShields.getInstance().getShield(j);
            if (shield.mainActivitySelection && shield.shieldType != null) {
                return true;
            }
        }
        return false;
    }
}
