package com.integreight.onesheeld;

import java.util.Set;

import com.integreight.onesheeld.utils.OneShieldTextView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArduinoConnectivityActivity extends Activity {

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
	private boolean isScanButton = true;
	private OneShieldTextView statusText;
	private LinearLayout transactionSlogan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.initialization_view);
		deviceListCont = (RelativeLayout) findViewById(R.id.devicesListContainer);
		loading = (ProgressBar) findViewById(R.id.progress);
		scanOrTryAgain = (Button) findViewById(R.id.scanOrTryAgain);
		statusText = (OneShieldTextView) findViewById(R.id.statusText);
		transactionSlogan = (LinearLayout) findViewById(R.id.transactionSlogan);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		setScanButtonReady();
		scanOrTryAgain.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isScanButton) {
					showProgress();
					changeSlogan("Searching.....", COLOR.RED);
					scanDevices();
					doDiscovery();
				} else {
					showProgress();
					changeSlogan("Connecting.....", COLOR.GREEN);
				}
			}
		});
		super.onCreate(savedInstanceState);
	}

	private void setScanButtonReady() {
		changeSlogan("Scan for 1Sheeld", COLOR.RED);
		deviceListCont.setVisibility(View.INVISIBLE);
		loading.setVisibility(View.INVISIBLE);
		scanOrTryAgain.setVisibility(View.VISIBLE);
		isScanButton = true;
	}

	private void setRetryButtonReady(String msg, View.OnClickListener onClick) {
		deviceListCont.setVisibility(View.INVISIBLE);
		loading.setVisibility(View.INVISIBLE);
		scanOrTryAgain.setVisibility(View.VISIBLE);
		changeSlogan(msg, COLOR.ORANGE);
		scanOrTryAgain.setOnClickListener(onClick);
		isScanButton = false;
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
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
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
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

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
			changeSlogan("Select Your Device", COLOR.YELLOW);
		} else {
			setRetryButtonReady("Not Found", new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

				}
			});
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		try {
			this.unregisterReceiver(mReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
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
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
					setRetryButtonReady("Not Connected", new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

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
