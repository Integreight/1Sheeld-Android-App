package com.integreight.onesheeld;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.ConnectionDetector;

public class OneSheeldActivity extends SherlockActivity {

	List<UIShield> shieldsUIList;
	ShieldsListAdapter adapter;
	ListView shieldsListView;
	MenuItem bluetoothSearchActionButton;
	MenuItem bluetoothDisconnectActionButton;
	MenuItem goToShieldsOperationActionButton;

	private static final String TAG = "OneSheeldActivity";
	private static final boolean D = true;

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 3;
	private static boolean arduinoConnected;

	private BluetoothAdapter mBluetoothAdapter = null;

	SharedPreferences sharedPrefs;

	// Internet Connection detector
	private ConnectionDetector cd;
// 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(OneSheeldService.COMMUNICAITON_ERROR)) {
				UIShield.setConnected(false);
				adapter.notifyDataSetChanged();
				arduinoConnected = false;
			} else if (action
					.equals(OneSheeldService.SHEELD_BLUETOOTH_CONNECTED)) {
				Log.e(TAG, "- ARDUINO CONNECTED -");
				if (isOneSheeldServiceRunning()) {
					arduinoConnected = true;
					setColoredStrips();
				}

				setSupportProgressBarIndeterminateVisibility(false);
			} else if (action.equals(OneSheeldService.SHEELD_CLOSE_CONNECTION)) {
				arduinoConnected = false;
				setBWStrips();
				setSupportProgressBarIndeterminateVisibility(false);

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		shieldsUIList = Arrays.asList(UIShield.values());
		shieldsListView = (ListView) findViewById(R.id.main_activity_shields_listview);
		shieldsListView.setEnabled(false);
		adapter = new ShieldsListAdapter(this);
		shieldsListView.setAdapter(adapter);
		shieldsListView.setCacheColorHint(Color.TRANSPARENT);
		shieldsListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		shieldsListView.setDrawingCacheEnabled(true);
		shieldsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> lv, View item, int position,
					long id) {
				RelativeLayout rLayout = (RelativeLayout) item;
				ToggleButton selectionMark = (ToggleButton) rLayout
						.getChildAt(0);
				ImageView selectionCircle = (ImageView) rLayout.getChildAt(1);

				if (selectionMark.isChecked()) {
					selectionMark.setChecked(false);
					selectionMark.setVisibility(View.INVISIBLE);
					selectionCircle.setVisibility(View.INVISIBLE);
					UIShield.getItem(position + 1).setMainActivitySelection(
							false);
				} else {
					selectionMark.setChecked(true);
					selectionMark.setVisibility(View.VISIBLE);
					selectionCircle.setVisibility(View.VISIBLE);
					UIShield.getItem(position + 1).setMainActivitySelection(
							true);

				}
			}

		});

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(OneSheeldService.COMMUNICAITON_ERROR));
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(OneSheeldService.SHEELD_BLUETOOTH_CONNECTED));
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(OneSheeldService.SHEELD_CLOSE_CONNECTION));

		sharedPrefs = this.getSharedPreferences("com.integreight.onesheeld",
				Context.MODE_PRIVATE);

		// Check if Internet present
		cd = new ConnectionDetector(getApplicationContext());
		if (!cd.isConnectingToInternet()) {
			Toast.makeText(this,
					"Please connect to working Internet connection",
					Toast.LENGTH_SHORT).show();
			// stop executing code by return
			return;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		if (!isOneSheeldServiceRunning()) {
			setBWStrips();
			if (sharedPrefs.contains(OneSheeldService.DEVICE_ADDRESS_KEY)) {
				setSupportProgressBarIndeterminateVisibility(true);
				Intent intent = new Intent();
				intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS,
						sharedPrefs.getString(
								OneSheeldService.DEVICE_ADDRESS_KEY, null));
				connectDevice(intent);
			}

		} else {
			setColoredStrips();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				setSupportProgressBarIndeterminateVisibility(true);
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data) {

		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		Intent intent = new Intent(this, OneSheeldService.class);
		intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
		startService(intent);

	}

	private void disconnectService() {
		// if (isOneSheeldServiceRunning()) {
		stopService(new Intent(this, OneSheeldService.class));
		setBWStrips();
		// }
	}

	private void launchShieldsOperationActivity() {
		if (!isAnyShieldsSelected()) {
			Toast.makeText(this, "Select at least 1 shield", Toast.LENGTH_LONG)
					.show();
			return;
		}
		Intent shieldsActivity = new Intent(OneSheeldActivity.this,
				ShieldsOperationActivity.class);
		startActivity(shieldsActivity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		bluetoothSearchActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_search);
		bluetoothDisconnectActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_disconnect);
		goToShieldsOperationActionButton = (MenuItem) menu
				.findItem(R.id.main_activity_action_forward);

		if (!arduinoConnected || !isOneSheeldServiceRunning())
			changeActionIconsToDisconnected();
		else
			changeActionIconsToConnected();

		return true;
	}

	public void addMoreShields(View v) {
		// List<UIShield> tempShieldsList = new ArrayList<UIShield>();
		// for (UIShield shield : Arrays.asList(UIShield.values())) {
		// if (shield.isMainActivitySelection())
		// tempShieldsList.add(shield);
		// }
		// if (tempShieldsList.isEmpty())
		// return;
		// Intent buttonsActivityIntent = new Intent(MainActivity.this,
		// ShieldsOperationActivity.class);
		// startActivity(buttonsActivityIntent);

		Toast.makeText(this, "Coming very soon!", Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.flash_bootloader_menuitem:
			Intent intent = new Intent(MainActivity.this,
			Bootloader.class);
			startActivity(intent);
			return true;
		case R.id.main_activity_action_search:
			// Launch the DeviceListActivity to see devices and do scan

			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.main_activity_action_disconnect:
			disconnectService();
			return true;
		case R.id.main_activity_action_forward:
			launchShieldsOperationActivity();
			return true;
		}

		return false;
	}

	private boolean isOneSheeldServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (OneSheeldService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void setColoredStrips() {
		UIShield.setConnected(true);
		adapter.notifyDataSetChanged();
		shieldsListView.setEnabled(true);
		changeActionIconsToConnected();
	}

	private void setBWStrips() {
		UIShield.setConnected(false);
		adapter.notifyDataSetChanged();
		shieldsListView.setEnabled(false);
		changeActionIconsToDisconnected();
	}

	private boolean isAnyShieldsSelected() {
		for (UIShield shield : shieldsUIList) {
			if (shield.isMainActivitySelection())
				return true;
		}
		return false;
	}

	private void changeActionIconsToConnected() {
		if (bluetoothSearchActionButton != null)
			bluetoothSearchActionButton.setVisible(false);
		if (bluetoothDisconnectActionButton != null)
			bluetoothDisconnectActionButton.setVisible(true);
		if (goToShieldsOperationActionButton != null)
			goToShieldsOperationActionButton.setVisible(true);
	}

	private void changeActionIconsToDisconnected() {
		if (bluetoothSearchActionButton != null)
			bluetoothSearchActionButton.setVisible(true);
		if (bluetoothDisconnectActionButton != null)
			bluetoothDisconnectActionButton.setVisible(false);
		if (goToShieldsOperationActionButton != null)
			goToShieldsOperationActionButton.setVisible(false);
	}

}
