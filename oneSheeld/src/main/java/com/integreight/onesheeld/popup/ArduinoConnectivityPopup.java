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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.HttpRequest;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.TimeOut;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Hashtable;

public class ArduinoConnectivityPopup extends Dialog {
    private Activity activity;
    private float scale;
    private boolean isConnecting = false;
    private Hashtable<String, BluetoothDevice> foundDevicesTable;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";

    public ArduinoConnectivityPopup(Activity context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = context;
        scale = activity.getResources().getDisplayMetrics().density;
        foundDevicesTable = new Hashtable<String, BluetoothDevice>();
    }

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private RelativeLayout deviceListCont;
    private LinearLayout devicesList;
    private ProgressBar loading;
    private Button scanOrTryAgain;
    private OneSheeldTextView statusText;
    private RelativeLayout transactionSlogan;
    public static boolean isOpened = false, backPressed = false;
    private TimeOut lockerTimeOut;

    @Override
    public void onBackPressed() {
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            setScanButtonReady();
        } else if (isConnecting) {
            if (((OneSheeldApplication) activity.getApplication())
                    .getAppFirmata() != null
                    && ((OneSheeldApplication) activity.getApplication())
                    .getAppFirmata().getBTService() != null) {
                try {
                    ((OneSheeldApplication) activity.getApplication())
                            .getAppFirmata().getBTService().stopConnection();
                } catch (Exception e) {
                    Log.e("TAG", "Exception", e);
                }
            }
            setDevicesListReady();
            changeSlogan(
                    activity.getResources()
                            .getString(R.string.selectYourDevice), COLOR.YELLOW);
        } else if (scanOrTryAgain.getVisibility() != View.VISIBLE
                || !scanOrTryAgain
                .getText()
                .toString()
                .equalsIgnoreCase(
                        activity.getResources()
                                .getString(R.string.scan)))
            setScanButtonReady();
        else {
            ((MainActivity) activity).finishManually();
            dismiss();
            cancel();
        }
        if (lockerTimeOut != null)
            lockerTimeOut.stopTimer();
        backPressed = true;
        // super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#CC000000"));
        }
        setContentView(R.layout.initialization_view);
        setCancelable(false);
        deviceListCont = (RelativeLayout) findViewById(R.id.devicesListContainer);
        loading = (ProgressBar) findViewById(R.id.progress);
        scanOrTryAgain = (Button) findViewById(R.id.scanOrTryAgain);
        statusText = (OneSheeldTextView) findViewById(R.id.statusText);
        transactionSlogan = (RelativeLayout) findViewById(R.id.transactionSlogan);
        devicesList = (LinearLayout) findViewById(R.id.devicesList);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        setScanButtonReady();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                isOpened = false;
                // Make sure we're not doing discovery anymore
                if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }
                ((OneSheeldApplication) activity.getApplication())
                        .getAppFirmata().removeEventHandler(
                        connectivityFirmataHandler);
                // Unregister broadcast listeners
                try {
                    activity.unregisterReceiver(mReceiver);
                } catch (Exception e) {
                    Log.e("TAG", "Exception", e);
                }
            }
        });
        if (mBtAdapter == null) {
            Toast.makeText(activity, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }
        ((PullToRefreshScrollView) findViewById(R.id.scrollingDevices))
                .setOnRefreshListener(new OnRefreshListener<ScrollView>() {

                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ScrollView> refreshView) {
                        ((PullToRefreshScrollView) findViewById(R.id.scrollingDevices))
                                .onRefreshComplete();
                        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
                            mBtAdapter.cancelDiscovery();
                        }
                        try {
                            activity.unregisterReceiver(mReceiver);
                        } catch (Exception e) {
                            Log.e("TAG", "Exception", e);
                        }
                        if (!mBtAdapter.isEnabled()) {
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
                                                                                    R.string.searching),
                                                                    COLOR.RED);
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
                                            R.string.searching), COLOR.RED);
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
                    public void onFailure(int arg0, Header[] arg1, String arg2,
                                          Throwable arg3) {
                        ((OneSheeldApplication) activity.getApplication())
                                .setMajorVersion(-1);
                        ((OneSheeldApplication) activity.getApplication())
                                .setMinorVersion(-1);
                        super.onFailure(arg0, arg1, arg2, arg3);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        isOpened = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        isOpened=false;
        super.onStop();
    }

    private void setScanButtonReady() {
        changeSlogan("Scan for 1Sheeld", COLOR.RED);
        isConnecting = false;
        deviceListCont.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.INVISIBLE);
        scanOrTryAgain.setVisibility(View.VISIBLE);
        scanOrTryAgain.setText(R.string.scan);
        scanOrTryAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
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
                                                                    R.string.searching),
                                                    COLOR.RED);
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
                    backPressed = false;
                    if (mBtAdapter != null && mBtAdapter.isDiscovering())
                        mBtAdapter.cancelDiscovery();
                    showProgress();
                    changeSlogan(
                            activity.getResources().getString(
                                    R.string.searching), COLOR.RED);
                    scanDevices();
                    doDiscovery();
                }
            }
        });
    }

    private void setRetryButtonReady(String msg, View.OnClickListener onClick) {
        isConnecting = false;
        if (backPressed == false) {
            deviceListCont.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.INVISIBLE);
            scanOrTryAgain.setVisibility(View.VISIBLE);
            changeSlogan(msg, COLOR.ORANGE);
            scanOrTryAgain.setText(R.string.tryAgain);
        }
    }

    private void setDevicesListReady() {
        deviceListCont.setVisibility(View.VISIBLE);
        loading.setVisibility(View.INVISIBLE);
        scanOrTryAgain.setVisibility(View.INVISIBLE);
    }

    private void showProgress() {
        deviceListCont.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
        scanOrTryAgain.setVisibility(View.INVISIBLE);
    }

    private void changeSlogan(String text, int color) {
        statusText.setText(text);
        transactionSlogan.setBackgroundColor(color);
    }

    Handler tempHandler = new Handler();

    private void initTimeOut() {
        if (lockerTimeOut != null)
            lockerTimeOut.stopTimer();
        lockerTimeOut = new TimeOut(10, new TimeOut.TimeoutHandler() {

            @Override
            public void onTimeout() {
                if (!mBtAdapter.isDiscovering()) {
                    if (foundDevicesTable.size() == 0) {
                        loading.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                setRetryButtonReady(activity.getResources()
                                                .getString(R.string.none_found),
                                        new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                scanDevices();
                                            }
                                        });
                            }
                        });
                    } else {
                        loading.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                // devicesList.removeAllViews();
                                for (int i = 0; i < devicesList.getChildCount(); i++) {
                                    OneSheeldTextView deviceView = (OneSheeldTextView) devicesList
                                            .getChildAt(i);
                                    BluetoothDevice btDevice = foundDevicesTable
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
                                            BluetoothDevice device = foundDevicesTable
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
                                                        device.getBondState() == BluetoothDevice.BOND_BONDED);
                                        }
                                    });
                                }
                                foundDevicesTable.clear();
                                if (devicesList.getChildCount() == 0) {
                                    setRetryButtonReady(activity.getResources()
                                                    .getString(R.string.none_found),
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
                } else
                    initTimeOut();
            }

            @Override
            public void onTick(int secondsLeft) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void scanDevices() {
        devicesList.removeAllViews();
        backPressed = false;
        foundDevicesTable = new Hashtable<String, BluetoothDevice>();
        // Register for broadcasts when a device is discovered
        initTimeOut();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver(mReceiver, filter);
    }

    ArduinoFirmataEventHandler connectivityFirmataHandler = new ArduinoFirmataEventHandler() {

        @Override
        public void onError(String errorMessage) {
            if (isOpened) {
                isConnecting = false;
                setRetryButtonReady(
                        activity.getResources()
                                .getString(R.string.notConnected),
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                scanDevices();
                            }
                        });
            }
        }

        @Override
        public void onConnect() {
            if (isOpened) {
                isConnecting = false;
                cancel();
            }
        }

        @Override
        public void onClose(boolean closedManually) {
            if (isOpened) {
                isConnecting = false;
                setRetryButtonReady(
                        activity.getResources()
                                .getString(R.string.notConnected),
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                scanDevices();
                            }
                        });
            }

        }
    };

    private void startService(String address, String name) {
        if (!isConnecting) {
            isConnecting = true;
            if (mBtAdapter != null && mBtAdapter.isDiscovering())
                mBtAdapter.cancelDiscovery();
            if (lockerTimeOut != null)
                lockerTimeOut.stopTimer();
            // Get the device MAC address, which is the last 17 chars in
            // the
            // View
            showProgress();
            ((OneSheeldApplication) activity.getApplication()).getAppFirmata()
                    .addEventHandler(connectivityFirmataHandler);
            changeSlogan(
                    activity.getResources().getString(R.string.connecting),
                    COLOR.GREEN);
            Intent intent = new Intent(activity, OneSheeldService.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_DEVICE_NAME, name);
            activity.startService(intent);
            isConnecting = true;
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private synchronized void doDiscovery() {
        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private void addFoundDevice(String name1, final String address,
                                boolean isPaired) {
        if (name1 == null)
            name1 = "";
        final String name=name1;
        if (name.trim().length() > 0
                && (name.toLowerCase().contains("1sheeld") || address
                .equals(name))
                && devicesList.findViewWithTag(address) == null) {
            if (((OneSheeldApplication) activity.getApplication())
                    .getLastConnectedDevice() != null
                    && ((OneSheeldApplication) activity.getApplication())
                    .getLastConnectedDevice().equals(address)) {
                startService(address,name);
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
                        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
                            backPressed = false;
                            if (((Checkable) findViewById(R.id.doAutomaticConnectionToThisDeviceCheckBox))
                                    .isChecked())
                                ((OneSheeldApplication) activity
                                        .getApplication())
                                        .setLastConnectedDevice(address);
                            startService(address,name);
                        } else {
                            if (mBtAdapter != null
                                    && mBtAdapter.isDiscovering())
                                mBtAdapter.cancelDiscovery();
                            if (lockerTimeOut != null)
                                lockerTimeOut.stopTimer();
                            ((MainActivity) activity)
                                    .setOnConnectToBluetooth(new onConnectedToBluetooth() {

                                        @Override
                                        public void onConnect() {
                                            backPressed = false;
                                            if (((Checkable) findViewById(R.id.doAutomaticConnectionToThisDeviceCheckBox))
                                                    .isChecked())
                                                ((OneSheeldApplication) activity
                                                        .getApplication())
                                                        .setLastConnectedDevice(address);
                                            startService(address,name);
                                        }
                                    });
                            Intent enableIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            activity.startActivityForResult(enableIntent,
                                    SheeldsList.REQUEST_ENABLE_BT);
                        }
                    }
                });
                devicesList.addView(item);
                setDevicesListReady();
                changeSlogan(
                        activity.getResources().getString(
                                R.string.selectYourDevice), COLOR.YELLOW);
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
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                addingDevicesHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent
                                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        lockerTimeOut.resetTimer();
                        foundDevicesTable.put(device.getAddress(), device);
                        addFoundDevice(
                                device.getName(),
                                device.getAddress(),
                                device.getBondState() == BluetoothDevice.BOND_BONDED);
                    }
                });
            }
        }
    };

    public static interface onConnectedToBluetooth {
        public void onConnect();
    }

    private final static class COLOR {
        public final static int RED = 0xff9B1201;
        public final static int YELLOW = 0xffE79401;
        public final static int GREEN = 0xff388813;
        public final static int ORANGE = 0xffE74D01;
    }
}
