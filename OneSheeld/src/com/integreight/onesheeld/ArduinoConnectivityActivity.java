package com.integreight.onesheeld;

import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.OneShieldTextView;

public class ArduinoConnectivityActivity extends Dialog {
	private Activity activity;

	public ArduinoConnectivityActivity(Activity context) {
		super(context, Window.FEATURE_NO_TITLE);
		this.activity = context;
		// TODO Auto-generated constructor stub
	}

	private static final String TAG = "DeviceListActivity";
	private static final boolean isTrue = true;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private RelativeLayout deviceListCont;
	private ProgressBar loading;
	private Button scanOrTryAgain;
//	private boolean isScanButton = true;
	private OneShieldTextView statusText;
	private RelativeLayout transactionSlogan;
	public static boolean isOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.initialization_view);
		deviceListCont = (RelativeLayout) findViewById(R.id.devicesListContainer);
		loading = (ProgressBar) findViewById(R.id.progress);
		scanOrTryAgain = (Button) findViewById(R.id.scanOrTryAgain);
		statusText = (OneShieldTextView) findViewById(R.id.statusText);
		transactionSlogan = (RelativeLayout) findViewById(R.id.transactionSlogan);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		setScanButtonReady();
		getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				isOpened = false;
				// Make sure we're not doing discovery anymore
				if (mBtAdapter != null) {
					mBtAdapter.cancelDiscovery();
				}

				// Unregister broadcast listeners
				try {
					activity.unregisterReceiver(mReceiver);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		isOpened = true;
		super.onStart();
	}

	private void setScanButtonReady() {
		changeSlogan("Scan for 1Sheeld", COLOR.RED);
		deviceListCont.setVisibility(View.INVISIBLE);
		loading.setVisibility(View.INVISIBLE);
		scanOrTryAgain.setVisibility(View.VISIBLE);
		scanOrTryAgain.setText(R.string.scan);
		scanOrTryAgain.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showProgress();
				changeSlogan(
						activity.getResources().getString(R.string.searching),
						COLOR.RED);
				scanDevices();
				doDiscovery();
			}
		});
	}

	private void setRetryButtonReady(String msg, View.OnClickListener onClick) {
		deviceListCont.setVisibility(View.INVISIBLE);
		loading.setVisibility(View.INVISIBLE);
		scanOrTryAgain.setVisibility(View.VISIBLE);
		changeSlogan(msg, COLOR.ORANGE);
		scanOrTryAgain.setText(R.string.tryAgain);
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

	private void scanDevices() {
		// setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(activity,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(activity,
				R.layout.device_name);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.devicesList);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		// ListView newDevicesListView = (ListView)
		// findViewById(R.id.devicesList);
		// pairedListView.setAdapter(mNewDevicesArrayAdapter);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		activity.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			// findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
			changeSlogan(
					activity.getResources()
							.getString(R.string.selectYourDevice), COLOR.YELLOW);
		} else {
			setRetryButtonReady(
					activity.getResources().getString(R.string.none_found),
					new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							scanDevices();
						}
					});
			String noDevices = activity.getResources()
					.getText(R.string.none_paired).toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	private void startService(String address) {
		// isBoundService = OneSheeldService.isBound;
		// if (!OneSheeldService.isBound) {
		Intent intent = new Intent(activity, OneSheeldService.class);
		intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
		activity.startService(intent);
		// stopService(intent);
		// activity.bindService(intent, ((OneSheeldApplication) activity
		// .getApplication()).getmConnection(),
		// Context.BIND_AUTO_CREATE);
		// }
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (isTrue)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		// setProgressBarIndeterminateVisibility(true);
		// setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		// findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			showProgress();
			changeSlogan(
					activity.getResources().getString(R.string.connecting),
					COLOR.GREEN);
			((OneSheeldApplication) activity.getApplication())
					.setArduinoFirmataHandlerForConnectivityPopup(new ArduinoFirmataEventHandler() {

						@Override
						public void onError(String errorMessage) {
							setRetryButtonReady(activity.getResources()
									.getString(R.string.notConnected),
									new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											scanDevices();
										}
									});

						}

						@Override
						public void onConnect() {
							cancel();
							Toast.makeText(activity, "Connected, finish",
									Toast.LENGTH_LONG).show();
						}

						@Override
						public void onClose(boolean closedManually) {
							setRetryButtonReady(activity.getResources()
									.getString(R.string.notConnected),
									new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											scanDevices();
										}
									});

						}
					});
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			startService(address);
			// Create the result Intent and include the MAC address
			// Intent intent = new Intent();
			// intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			//
			// // Set result and finish activity Activity
			// setResult(Activity.RESULT_OK, intent);
			// finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				setDevicesListReady();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// setProgressBarIndeterminateVisibility(false);
				// setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = activity.getResources()
							.getText(R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
					setRetryButtonReady(
							activity.getResources().getString(
									R.string.none_found),
							new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									scanDevices();
								}
							});
				}
			}
		}
	};

	private final static class COLOR {
		public final static int RED = 0xff9B1201;
		public final static int YELLOW = 0xffE79401;
		public final static int GREEN = 0xff388813;
		public final static int ORANGE = 0xffE74D01;
	}
}
