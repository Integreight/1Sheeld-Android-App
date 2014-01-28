package com.integreight.onesheeld.appFragments;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.ControllerParent;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class SheeldsList extends SherlockFragment {
	View v;
	boolean isInflated = false;
	private ListView shieldsListView;
	private static SheeldsList thisInstance;
	private List<UIShield> shieldsUIList;
	private ShieldsListAdapter adapter;
	private MenuItem bluetoothSearchActionButton;
	private MenuItem bluetoothDisconnectActionButton;
	private MenuItem goToShieldsOperationActionButton;

	private static final String TAG = "ShieldsList";
	private static final boolean D = true;

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 3;
	private static boolean arduinoConnected;

	private BluetoothAdapter mBluetoothAdapter = null;

	public static SheeldsList getInstance() {
		if (thisInstance == null) {
			thisInstance = new SheeldsList();
		}
		return thisInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.app_sheelds_list, container, false);
		return v;
	}

	@Override
	public void onResume() {
		((MainActivity) getActivity()).getSlidingMenu().setTouchModeAbove(
				SlidingMenu.TOUCHMODE_NONE);
		// List<Fragment> frags = getActivity().getSupportFragmentManager()
		// .getFragments();
		// for (Fragment frag : frags) {
		// if (frag != null
		// && !frag.getClass().getName()
		// .equals(SheeldsList.class.getName())) {
		// FragmentTransaction ft = getActivity()
		// .getSupportFragmentManager().beginTransaction();
		// ft.remove(frag);
		// ft.commit();
		// }
		// }
		getActivity().setTitle("OneSheeld");
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setRetainInstance(true);
		initView();
		super.onActivityCreated(savedInstanceState);
	}

	private void initView() {
		shieldsListView = (ListView) getView().findViewById(R.id.sheeldsList);
		shieldsUIList = Arrays.asList(UIShield.values());
		shieldsListView.setEnabled(false);
		adapter = new ShieldsListAdapter(getActivity());
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
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity(), "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			getActivity().finish();
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				((MainActivity) getActivity())
						.setSupportProgressBarIndeterminateVisibility(true);
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {
				Log.d(TAG, "BT not enabled");
				Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void connectDevice(Intent data) {

		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		Intent intent = new Intent(getActivity(), OneSheeldService.class);
		intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
		getActivity().startService(intent);

	}

	private void disconnectService() {
		// if (isOneSheeldServiceRunning()) {
		getActivity().stopService(
				new Intent(getActivity(), OneSheeldService.class));
		setBWStrips();
		// }
	}

	private void launchShieldsOperationActivity() {
		if (!isAnyShieldsSelected()) {
			Toast.makeText(getActivity(), "Select at least 1 shield",
					Toast.LENGTH_LONG).show();
			return;
		}
		// Intent shieldsActivity = new Intent(getActivity(),
		// ShieldsOperationActivity.class);
		// startActivity(shieldsActivity);
		((MainActivity) getActivity()).replaceCurrentFragment(
				ShieldsOperations.getInstance(), "operations", true);
	}

	@Override
	public void onStart() {
		((MainActivity) getActivity())
				.setArduinoFirmataHandler(new ArduinoFirmataEventHandler() {

					@Override
					public void onError(String errorMessage) {
						UIShield.setConnected(false);
						adapter.notifyDataSetChanged();
						arduinoConnected = false;
					}

					@Override
					public void onConnect() {
						Log.e(TAG, "- ARDUINO CONNECTED -");
						if (isOneSheeldServiceRunning()) {
							arduinoConnected = true;
							setColoredStrips();
						}
						if (getActivity() != null)
							((MainActivity) getActivity())
									.setSupportProgressBarIndeterminateVisibility(false);
					}

					@Override
					public void onClose(boolean closedManually) {
						arduinoConnected = false;
						setBWStrips();
						if (getActivity() != null)
							((MainActivity) getActivity())
									.setSupportProgressBarIndeterminateVisibility(false);
					}
				});

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

		}

		if (!isOneSheeldServiceRunning()) {
			setBWStrips();
			if (((OneSheeldApplication) getActivity().getApplication())
					.getAppPreferences().contains(
							OneSheeldService.DEVICE_ADDRESS_KEY)) {
				((MainActivity) getActivity())
						.setSupportProgressBarIndeterminateVisibility(true);
				Intent intent = new Intent();
				intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS,
						((OneSheeldApplication) getActivity().getApplication())
								.getLastConnectedDevice());
				connectDevice(intent);
			}

		} else {
			setColoredStrips();
		}
		super.onStart();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflate.inflate(R.menu.main, menu);
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.main_activity_action_search:
			// Launch the DeviceListActivity to see devices and do scan

			serverIntent = new Intent(getActivity(), DeviceListActivity.class);
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
		if (getActivity() != null) {
			ActivityManager manager = (ActivityManager) getActivity()
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
		int i = 0;
		OneSheeldApplication app = (OneSheeldApplication) getActivity()
				.getApplication();
		app.setRunningSheelds(new Hashtable<String, ControllerParent<?>>());
		for (UIShield shield : shieldsUIList) {
			if (shield.isMainActivitySelection()
					&& shield.getShieldType() != null) {
				ControllerParent<?> type = null;
				try {
					type = shield.getShieldType().newInstance();
				} catch (java.lang.InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				type.setActivity((MainActivity) getActivity()).setTag(
						shield.getName());
				i++;
			}
		}
		if (i > 0)
			return true;
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
