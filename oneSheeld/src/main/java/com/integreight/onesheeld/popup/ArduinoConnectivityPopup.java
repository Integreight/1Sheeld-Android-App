package com.integreight.onesheeld.popup;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.utils.HttpRequest;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.URLSpanNoUnderline;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ArduinoConnectivityPopup extends Dialog {
    private Activity activity;
    public static ArduinoConnectivityPopup thisInstance;
    private float scale;
    private boolean isConnecting = false;
    private Hashtable<String, OneSheeldDevice> foundDevicesTable;
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static final String IS_BUY_TEXT_ENABLED_SP = "com.integreight.onesheeld.IS_BUY_TEXT_ENABLED_SP";


    public ArduinoConnectivityPopup(Activity context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = context;
        scale = activity.getResources().getDisplayMetrics().density;
        foundDevicesTable = new Hashtable<>();
        thisInstance = this;
    }

    // Member fields
    private RelativeLayout deviceListCont;
    private LinearLayout devicesList;
    private ProgressBar loading, smallLoading;
    private Button scanOrTryAgain;
    private OneSheeldTextView statusText;
    private OneSheeldTextView buy1SheeldBoardTextView;
    private OneSheeldButton skipScan;
    private RelativeLayout transactionSlogan;
    public static boolean isOpened = false, backPressed = false;
    private boolean isScanningFinishedManually = false;

    @Override
    public void onBackPressed() {
        if (OneSheeldSdk.getManager().isScanning()) {
            isScanningFinishedManually = true;
            OneSheeldSdk.getManager().cancelScanning();
            setScanButtonReady();
        } else if (OneSheeldSdk.getManager().isConnecting()) {
            isConnecting = false;
            OneSheeldSdk.getManager().cancelConnecting();
            setDevicesListReady();
            changeSlogan(
                    activity.getResources()
                            .getString(R.string.connectivity_popup_select_your_device), COLOR.YELLOW);
            findViewById(R.id.skip_scan).setVisibility(View.VISIBLE);
        } else if (scanOrTryAgain.getVisibility() != View.VISIBLE
                || !scanOrTryAgain
                .getText()
                .toString()
                .equalsIgnoreCase(
                        activity.getResources()
                                .getString(R.string.connectivity_popup_scan_button)))
            setScanButtonReady();
        else {
            ((MainActivity) activity).finishManually();
            dismiss();
            cancel();
        }
        backPressed = true;
        // super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#CC000000"));
        }
        setContentView(R.layout.initialization_view);
        setCancelable(false);
        ((OneSheeldApplication)activity.getApplication()).setIsDemoMode(false);
        deviceListCont = (RelativeLayout) findViewById(R.id.devicesListContainer);
        loading = (ProgressBar) findViewById(R.id.progress);
        smallLoading = (ProgressBar) findViewById(R.id.small_progress);
        scanOrTryAgain = (Button) findViewById(R.id.scanOrTryAgain);
        statusText = (OneSheeldTextView) findViewById(R.id.statusText);
        skipScan = (OneSheeldButton) findViewById(R.id.skip_scan);
        transactionSlogan = (RelativeLayout) findViewById(R.id.transactionSlogan);
        devicesList = (LinearLayout) findViewById(R.id.devicesList);
        buy1SheeldBoardTextView = (OneSheeldTextView) findViewById(R.id.buy_1sheeld_board_text_view);
        buy1SheeldBoardTextView.setMovementMethod(LinkMovementMethod.getInstance());
        tempHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    buy1SheeldBoardTextView.setText(((OneSheeldApplication) activity.getApplication()).isLocatedInTheUs()?
                            R.string.connectivity_popup_dont_have_the_board_get_it_now_US:
                            R.string.connectivity_popup_dont_have_the_board_get_it_now);
            }
        },1000);
        URLSpanNoUnderline.stripUnderlines(buy1SheeldBoardTextView);
        setScanButtonReady();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                isOpened = false;
                // Make sure we're not doing discovery anymore
                if (OneSheeldSdk.getManager().isScanning()) {
                    isScanningFinishedManually = true;
                    OneSheeldSdk.getManager().cancelScanning();
                }
                OneSheeldSdk.getManager().removeConnectionCallback(connectionCallback);
                OneSheeldSdk.getManager().removeErrorCallback(errorCallback);
                OneSheeldSdk.getManager().removeScanningCallback(scanningCallback);
                // Unregister broadcast listeners
                try {
                    activity.unregisterReceiver(mReceiver);
                } catch (Exception e) {
                    Log.e("TAG", "Exception", e);
                }
            }
        });
        skipScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OneSheeldApplication) activity.getApplication()).setIsDemoMode(true);
                ArduinoConnectivityPopup.isOpened = false;
                if (OneSheeldSdk.getManager().isScanning()) {
                    isScanningFinishedManually = true;
                    OneSheeldSdk.getManager().cancelScanning();
                    setScanButtonReady();
                }
                ArduinoConnectivityPopup.thisInstance.cancel();
                ((ViewGroup) activity.findViewById(R.id.cancelConnection)).getChildAt(1).setBackgroundResource(R.drawable.scan_button);
            }
        });
        ((PullToRefreshScrollView) findViewById(R.id.scrollingDevices))
                .setOnRefreshListener(new OnRefreshListener<ScrollView>() {

                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ScrollView> refreshView) {
                        ((PullToRefreshScrollView) findViewById(R.id.scrollingDevices))
                                .onRefreshComplete();
                        if (OneSheeldSdk.getManager().isScanning()) {
                            isScanningFinishedManually = true;
                            OneSheeldSdk.getManager().cancelScanning();
                        }
                        try {
                            activity.unregisterReceiver(mReceiver);
                        } catch (Exception e) {
                            Log.e("TAG", "Exception", e);
                        }
                        if (BluetoothAdapter.getDefaultAdapter()!=null &&!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            ((MainActivity) activity)
                                    .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                        @Override
                                        public void onConnect() {
                                            addingDevicesHandler
                                                    .post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            backPressed = false;
                                                            showProgress();
                                                            changeSlogan(
                                                                    activity.getResources()
                                                                            .getString(
                                                                                    R.string.connectivity_popup_searching) + "......",
                                                                    COLOR.RED);
                                                            findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                                                            scanDevices();
                                                            doDiscovery();
                                                        }
                                                    });
                                        }
                                    });
                            Intent enableIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            activity.startActivityForResult(enableIntent,
                                    SheeldsList.REQUEST_ENABLE_BT);
                        } else {
                            showProgress();
                            changeSlogan(
                                    activity.getResources().getString(
                                            R.string.connectivity_popup_searching) + "......", COLOR.RED);
                            findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                            scanDevices();
                            doDiscovery();
                        }
                    }
                });
        HttpRequest.getInstance().get(
                OneSheeldApplication.FIRMWARE_UPGRADING_URL,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {

                        ((OneSheeldApplication) activity.getApplication())
                                .setMajorVersion(-1);
                        ((OneSheeldApplication) activity.getApplication())
                                .setMinorVersion(-1);
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        try {
                            System.err.println(response);
                            ((OneSheeldApplication) activity.getApplication())
                                    .setMajorVersion(Integer.parseInt(response
                                            .getString("major")));
                            ((OneSheeldApplication) activity.getApplication())
                                    .setMinorVersion(Integer.parseInt(response
                                            .getString("minor")));
                            ((OneSheeldApplication) activity.getApplication())
                                    .setVersionWebResult(response.toString());
                        } catch (NumberFormatException e) {
                            // TODO Auto-generated catch block
                            Log.e("TAG", "Exception", e);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Log.e("TAG", "Exception", e);
                        }
                        super.onSuccess(statusCode, headers, response);
                    }
                });
        OneSheeldSdk.getManager().addConnectionCallback(connectionCallback);
        OneSheeldSdk.getManager().addErrorCallback(errorCallback);
        OneSheeldSdk.getManager().addScanningCallback(scanningCallback);
        handleBuyLinkVisibility();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        isOpened = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        isOpened = false;
        super.onStop();
    }

    private void setScanButtonReady() {
        changeSlogan(activity.getString(R.string.connectivity_popup_scan_for_1sheeld), COLOR.RED);
        findViewById(R.id.skip_scan).setVisibility(View.VISIBLE);
        isConnecting = false;
        deviceListCont.setVisibility(View.INVISIBLE);
        handleBuyLinkVisibility();
        loading.setVisibility(View.INVISIBLE);
        smallLoading.setVisibility(View.INVISIBLE);
        scanOrTryAgain.setVisibility(View.VISIBLE);
        scanOrTryAgain.setText(R.string.connectivity_popup_scan_button);
        scanOrTryAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (BluetoothAdapter.getDefaultAdapter()!=null && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    ((MainActivity) activity)
                            .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                @Override
                                public void onConnect() {
                                    addingDevicesHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            backPressed = false;
                                            showProgress();
                                            changeSlogan(
                                                    activity.getResources()
                                                            .getString(
                                                                    R.string.connectivity_popup_searching) + "......",
                                                    COLOR.RED);
                                            findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                                            scanDevices();
                                            doDiscovery();
                                        }
                                    });
                                }
                            });
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableIntent,
                            SheeldsList.REQUEST_ENABLE_BT);
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        backPressed = false;
                        if (OneSheeldSdk.getManager().isScanning()) {
                            isScanningFinishedManually = true;
                            OneSheeldSdk.getManager().cancelScanning();
                        }
                        showProgress();
                        changeSlogan(
                                activity.getResources().getString(
                                        R.string.connectivity_popup_searching) + "......", COLOR.RED);
                        findViewById(R.id.skip_scan).setVisibility(View.VISIBLE);
                        scanDevices();
                        doDiscovery();
                    } else {
                        if (((MainActivity) activity).checkForLocationPermission()) {
                            backPressed = false;
                            if (OneSheeldSdk.getManager().isScanning()) {
                                isScanningFinishedManually = true;
                                OneSheeldSdk.getManager().cancelScanning();
                            }
                            showProgress();
                            changeSlogan(
                                    activity.getResources().getString(
                                            R.string.connectivity_popup_searching) + "......", COLOR.RED);
                            findViewById(R.id.skip_scan).setVisibility(View.VISIBLE);
                            scanDevices();
                            doDiscovery();
                        } else {
                            ((MainActivity) activity)
                                    .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                        @Override
                                        public void onConnect() {
                                            addingDevicesHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    backPressed = false;
                                                    showProgress();
                                                    changeSlogan(
                                                            activity.getResources()
                                                                    .getString(
                                                                            R.string.connectivity_popup_searching) + "......",
                                                            COLOR.RED);
                                                    findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                                                    scanDevices();
                                                    doDiscovery();
                                                }
                                            });
                                        }
                                    });
                            ((MainActivity) activity).checkAndAskForLocationPermission();
                        }
                    }
                }
            }
        });
    }

    private void setRetryButtonReady(final String msg, final View.OnClickListener onClick) {
        loading.post(new Runnable() {
            @Override
            public void run() {

                isConnecting = false;
                if (backPressed == false) {
                    deviceListCont.setVisibility(View.INVISIBLE);
                    handleBuyLinkVisibility();
                    loading.setVisibility(View.INVISIBLE);
                    smallLoading.setVisibility(View.INVISIBLE);
                    scanOrTryAgain.setVisibility(View.VISIBLE);
                    changeSlogan(msg, COLOR.ORANGE);
                    findViewById(R.id.skip_scan).setVisibility(View.VISIBLE);
                    scanOrTryAgain.setText(R.string.connectivity_popup_try_again_button);
                }
            }
        });
    }

    private void setDevicesListReady() {
        loading.post(new Runnable() {
            @Override
            public void run() {
                deviceListCont.setVisibility(View.VISIBLE);
                handleBuyLinkVisibility();
                loading.setVisibility(View.INVISIBLE);
                smallLoading.setVisibility(View.INVISIBLE);
                scanOrTryAgain.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void handleBuyLinkVisibility(){
        boolean isTheTextViewEnabled = true;
        try{
            isTheTextViewEnabled=((OneSheeldApplication)activity.getApplication()).getAppPreferences().getBoolean(IS_BUY_TEXT_ENABLED_SP, true);
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
        if(deviceListCont.getVisibility()==View.INVISIBLE && !buy1SheeldBoardTextView.getText().equals("") && isTheTextViewEnabled){
            buy1SheeldBoardTextView.setVisibility(View.VISIBLE);
        }
        else{
            buy1SheeldBoardTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgress() {
        loading.post(new Runnable() {
            @Override
            public void run() {
                deviceListCont.setVisibility(View.INVISIBLE);
                handleBuyLinkVisibility();
                loading.setVisibility(View.VISIBLE);
                smallLoading.setVisibility(View.INVISIBLE);
                scanOrTryAgain.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void changeSlogan(final String text, final int color) {
        loading.post(new Runnable() {
            @Override
            public void run() {
                statusText.setText(text);
                transactionSlogan.setBackgroundColor(color);
            }
        });
    }

    Handler tempHandler = new Handler();


    private void scanDevices() {
        devicesList.removeAllViews();
        backPressed = false;
        foundDevicesTable = new Hashtable<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver(mReceiver, filter);
    }

    OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
        @Override
        public void onConnectionRetry(OneSheeldDevice device, int retryCount) {
            super.onConnectionRetry(device, retryCount);
        }

        @Override
        public void onDisconnect(OneSheeldDevice device) {
            super.onDisconnect(device);
            ((OneSheeldApplication) activity.getApplication()).setConnectedDevice(null);
            if (isOpened) {
                isConnecting = false;
                setRetryButtonReady(
                        activity.getResources()
                                .getString(R.string.connectivity_popup_not_connected),
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                scanDevices();
                            }
                        });
            }
        }

        @Override
        public void onConnect(OneSheeldDevice device) {
            super.onConnect(device);
            if (isOpened) {
                isConnecting = false;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ViewGroup) activity.findViewById(R.id.cancelConnection)).getChildAt(1).setBackgroundResource(R.drawable.bluetooth_disconnect_button);
                    }
                });
                cancel();
            }
            try{
                ((OneSheeldApplication)activity.getApplication()).getAppPreferences().edit().putBoolean(IS_BUY_TEXT_ENABLED_SP, false).apply();
            }
            catch (Exception ignored){
                ignored.printStackTrace();
            }

        }
    };
    OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
        @Override
        public void onScanStart() {
            super.onScanStart();
            addingDevicesHandler.post(new Runnable() {

                @Override
                public void run() {
                    showProgress();
                    changeSlogan(
                            activity.getResources().getString(
                                    R.string.connectivity_popup_searching) + "......", COLOR.RED);
                    findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                }
            });
        }

        @Override
        public void onDeviceFind(final OneSheeldDevice device) {
            super.onDeviceFind(device);
            addingDevicesHandler.post(new Runnable() {

                @Override
                public void run() {
                    // Get the BluetoothDevice object from the Intent
                    foundDevicesTable.put(device.getAddress(), device);
                    addFoundDevice(
                            device.getName(),
                            device.getAddress(),
                            device.isPaired());
                }
            });
        }

        @Override
        public void onScanFinish(List<OneSheeldDevice> foundDevices) {
            super.onScanFinish(foundDevices);
            if (!isScanningFinishedManually) {
                if (foundDevices.size() == 0) {
                    loading.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            smallLoading.setVisibility(View.INVISIBLE);
                            setRetryButtonReady(activity.getResources()
                                            .getString(R.string.connectivity_popup_no_devices_found),
                                    new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            scanDevices();
                                        }
                                    });
                        }
                    });
                } else {
                    foundDevicesTable.clear();
                    for(OneSheeldDevice device:foundDevices)
                    {
                        foundDevicesTable.put(device.getAddress(),device);
                    }
                    loading.post(new Runnable() {

                        @Override
                        public void run() {
                            smallLoading.setVisibility(View.INVISIBLE);
                            // devicesList.removeAllViews();
                            for (int i = 0; i < devicesList.getChildCount(); i++) {
                                OneSheeldTextView deviceView = (OneSheeldTextView) devicesList
                                        .getChildAt(i);
                                OneSheeldDevice btDevice = foundDevicesTable
                                        .get(deviceView.getTag());
                                if (btDevice != null) {
                                    if (btDevice.getName() != null
                                            && btDevice.getName()
                                            .toLowerCase()
                                            .contains("1sheeld")) {
                                        deviceView
                                                .setText(foundDevicesTable
                                                        .get(deviceView
                                                                .getTag())
                                                        .getName());
                                    } else {
                                        devicesList.removeView(deviceView);
                                    }
                                    foundDevicesTable.remove(deviceView
                                            .getTag());
                                }
                            }
                            final Enumeration<String> enumKey = foundDevicesTable
                                    .keys();
                            addingDevicesHandler
                                    .removeCallbacksAndMessages(null);
                            while (enumKey.hasMoreElements()) {
                                final String key = enumKey.nextElement();
                                tempHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        OneSheeldDevice device = foundDevicesTable
                                                .get(key);
                                        if (device != null)
                                            addFoundDevice(
                                                    device.getName() != null
                                                            && device
                                                            .getName()
                                                            .length() > 0 ? device
                                                            .getName()
                                                            : device.getAddress(),
                                                    key,
                                                    device.isPaired());
                                    }
                                });
                            }
                            foundDevicesTable.clear();
                            if (devicesList.getChildCount() == 0) {
                                setRetryButtonReady(activity.getResources()
                                                .getString(R.string.connectivity_popup_no_devices_found),
                                        new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                scanDevices();
                                            }
                                        });
                            }
                        }
                    });
                }
            }
            isScanningFinishedManually = false;
        }
    };
    OneSheeldErrorCallback errorCallback = new OneSheeldErrorCallback() {
        @Override
        public void onError(OneSheeldDevice device, OneSheeldError error) {
            super.onError(device, error);
            if (isOpened) {

                if (error == OneSheeldError.BLUETOOTH_NOT_ENABLED) {
                    ((MainActivity) activity)
                            .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                @Override
                                public void onConnect() {
                                    addingDevicesHandler
                                            .post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    backPressed = false;
                                                    showProgress();
                                                    changeSlogan(
                                                            activity.getResources()
                                                                    .getString(
                                                                            R.string.connectivity_popup_searching) + "......",
                                                            COLOR.RED);
                                                    findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
                                                    scanDevices();
                                                    doDiscovery();
                                                }
                                            });
                                }
                            });
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableIntent,
                            SheeldsList.REQUEST_ENABLE_BT);
                } else {
                    isConnecting = false;
                    setRetryButtonReady(
                            activity.getResources()
                                    .getString(R.string.connectivity_popup_not_connected),
                            new View.OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    scanDevices();
                                }
                            });
                }
            }
        }
    };

    private void startService(String address, String name) {
        if (!isConnecting) {
            isConnecting = true;
            if (OneSheeldSdk.getManager().isScanning()){
                isScanningFinishedManually = true;
                OneSheeldSdk.getManager().cancelScanning();
            }

            // Get the device MAC address, which is the last 17 chars in
            // the
            // View
            showProgress();
            changeSlogan(
                    activity.getResources().getString(R.string.connectivity_popup_connecting) + "......",
                    COLOR.GREEN);
            findViewById(R.id.skip_scan).setVisibility(View.INVISIBLE);
            OneSheeldSdk.getManager().connect(new OneSheeldDevice(address, name));
            isConnecting = true;
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private synchronized void doDiscovery() {
        // If we're already discovering, stop it

        if (OneSheeldSdk.getManager().isScanning()) {
            isScanningFinishedManually = true;
            OneSheeldSdk.getManager().cancelScanning();
        }

        // Request discover from BluetoothAdapter
        OneSheeldSdk.getManager().scan();
    }

    private void addFoundDevice(String name1, final String address,
                                boolean isPaired) {
        if (name1 == null)
            name1 = "";
        final String name = name1;
        if (name.trim().length() > 0
                && (name.toLowerCase().contains("1sheeld") || address
                .equals(name))
                && devicesList.findViewWithTag(address) == null) {
            if (((OneSheeldApplication) activity.getApplication())
                    .getLastConnectedDevice() != null
                    && ((OneSheeldApplication) activity.getApplication())
                    .getLastConnectedDevice().equals(address)) {
                startService(address, name);
            } else {
                OneSheeldTextView item = new OneSheeldTextView(activity, null);
                item.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                item.setText(name);
                item.setTag(address);
                item.setGravity(Gravity.CENTER_VERTICAL);
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                item.setTextColor(Color.WHITE);
                int pdng = (int) (8 * scale - .5f);
                item.setPadding(pdng, pdng, pdng, pdng);
                Drawable img = getContext()
                        .getResources()
                        .getDrawable(
                                isPaired ? R.drawable.arduino_connectivity_activity_onesheeld_small_green_logo
                                        : R.drawable.arduino_connectivity_activity_onesheeld_small_logo);
                item.setCompoundDrawablesWithIntrinsicBounds(img, null, null,
                        null);
                item.setBackgroundResource(R.drawable.devices_list_item_selector);
                item.setCompoundDrawablePadding(pdng);
                item.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                                                    backPressed = false;
                                                    if (((Checkable) findViewById(R.id.doAutomaticConnectionToThisDeviceCheckBox))
                                                            .isChecked())
                                                        ((OneSheeldApplication) activity
                                                                .getApplication())
                                                                .setLastConnectedDevice(address);
                                                    startService(address, name);
                                                } else {
                                                    if (OneSheeldSdk.getManager().isScanning()) {
                                                        isScanningFinishedManually = true;
                                                        OneSheeldSdk.getManager().cancelScanning();
                                                    }
                                                    ((MainActivity) activity)
                                                            .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                                                @Override
                                                                public void onConnect() {
                                                                    backPressed = false;
                                                                    ((OneSheeldApplication) activity.getApplication()).setIsDemoMode(false);
                                                                    if (((Checkable) findViewById(R.id.doAutomaticConnectionToThisDeviceCheckBox))
                                                                            .isChecked())
                                                                        ((OneSheeldApplication) activity
                                                                                .getApplication())
                                                                                .setLastConnectedDevice(address);
                                                                    startService(address, name);
                                                                }
                                                            });
                                                    Intent enableIntent = new Intent(
                                                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                                    activity.startActivityForResult(enableIntent,
                                                            SheeldsList.REQUEST_ENABLE_BT);
                                                }
                                            }
                                        }

                );
                devicesList.addView(item);

                setDevicesListReady();

                changeSlogan(
                        activity.getResources()

                                .

                                        getString(
                                                R.string.connectivity_popup_select_your_device), COLOR

                                .YELLOW);

                findViewById(R.id.skip_scan)

                        .

                                setVisibility(View.VISIBLE);

                smallLoading.setVisibility(View.VISIBLE);
            }
        }
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    Handler addingDevicesHandler = new Handler();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
                scanDevices();
        }
    };

    public interface onConnectedToBluetooth {
        void onConnect();
    }

    private final static class COLOR {
        public final static int RED = 0xff9B1201;
        public final static int YELLOW = 0xffE79401;
        public final static int GREEN = 0xff388813;
        public final static int ORANGE = 0xffE74D01;
    }
}
