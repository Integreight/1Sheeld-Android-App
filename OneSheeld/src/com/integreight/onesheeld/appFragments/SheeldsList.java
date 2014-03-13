package com.integreight.onesheeld.appFragments;

import java.util.Arrays;
import java.util.Enumeration;
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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.ArduinoConnectivityPopup;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.OneSheeldVersionInstallerPopup;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.ListViewReversed;
import com.integreight.onesheeld.utils.OneShieldEditText;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;

public class SheeldsList extends Fragment {
	View v;
	boolean isInflated = false;
	private ListViewReversed shieldsListView;
	private static SheeldsList thisInstance;
	private List<UIShield> shieldsUIList;
	private ShieldsListAdapter adapter;
	private MenuItem bluetoothSearchActionButton;
	private MenuItem bluetoothDisconnectActionButton;
	private MenuItem goToShieldsOperationActionButton;

	private static final String TAG = "ShieldsList";
	private static final boolean D = true;

	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 3;
	private static boolean arduinoConnected;

	// private BluetoothAdapter mBluetoothAdapter = null;

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
		// if (v == null)
		v = inflater.inflate(R.layout.app_sheelds_list, container, false);
		// else {
		// try {
		// ((ViewGroup) v.getParent()).removeView(v);
		// } catch (Exception e) {
		// // TODO: handle exception
		// }
		// if (shieldsListView != null)
		// shieldsListView.setSelection(1);
		// }
		return v;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		// getSherlockActivity().getSupportActionBar().hide();
		// ((MainActivity) getActivity()).getSlidingMenu().setTouchModeAbove(
		// SlidingMenu.TOUCHMODE_NONE);
		((MainActivity) getActivity()).disableMenu();
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Hashtable<String, ControllerParent<?>> controllers = ((OneSheeldApplication) getActivity()
						.getApplication()).getRunningShields();
				Enumeration<String> enumKey = controllers.keys();
				while (enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					controllers.get(key).reset();
				}
				ArduinoPin[] pins = ArduinoPin.values();
				for (int i = 0; i < pins.length; i++) {
					pins[i].connectedPins.clear();
				}
				((OneSheeldApplication) getActivity().getApplication())
						.clearServiceEventHandlers();
				ConnectingPinsView.getInstance().recycle();
			}
		});
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				List<Fragment> frags = getActivity()
						.getSupportFragmentManager().getFragments();
				for (Fragment frag : frags) {
					if (frag != null
							&& !frag.getClass().getName()
									.equals(SheeldsList.class.getName())) {
						FragmentTransaction ft = getActivity()
								.getSupportFragmentManager().beginTransaction();
						frag.onDestroy();
						ft.remove(frag);
						ft.commitAllowingStateLoss();
					}
				}
			}
		}, 500);
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
		getActivity().findViewById(R.id.getAvailableDevices)
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						launchShieldsOperationActivity();
					}
				});
		shieldsListView = (ListViewReversed) getView().findViewById(
				R.id.sheeldsList);
		shieldsUIList = Arrays.asList(UIShield.values());
		// shieldsListView.setEnabled(false);
		adapter = new ShieldsListAdapter(getActivity());
		shieldsListView.setAdapter(adapter);
		// if (isInflated)
		shieldsListView.setSelection(1);
		shieldsListView.setCacheColorHint(Color.TRANSPARENT);
		shieldsListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		shieldsListView.setDrawingCacheEnabled(true);
		final OneShieldEditText searchBox = (OneShieldEditText) shieldsListView
				.findViewById(R.id.searchArea);
		// searchBox.setAdapter(adapter);
		searchBox.setDropDownHeight(0);
		shieldsListView.findViewById(R.id.selectAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						for (int i = 0; i < UIShield.values().length; i++) {
							UIShield.getPosition(i + 1)
									.setMainActivitySelection(true);
						}
						adapter.selectAll();
					}
				});
		shieldsListView.findViewById(R.id.reset).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						for (int i = 0; i < UIShield.values().length; i++) {
							UIShield.getPosition(i + 1)
									.setMainActivitySelection(false);
						}
						adapter.reset();
					}
				});
		// shieldsListView.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> lv, View item, int position,
		// long id) {
		// RelativeLayout rLayout = (RelativeLayout) item;
		// ToggleButton selectionMark = (ToggleButton) rLayout
		// .getChildAt(0);
		// ImageView selectionCircle = (ImageView) rLayout.getChildAt(1);
		// ImageView upperBlackLayer = (ImageView) rLayout
		// .findViewById(R.id.shildListItemBlackSquare);
		//
		// if (selectionMark.isChecked()) {
		// selectionMark.setChecked(false);
		// selectionMark.setVisibility(View.INVISIBLE);
		// selectionCircle.setVisibility(View.INVISIBLE);
		// upperBlackLayer.setVisibility(View.VISIBLE);
		// UIShield.getPosition(position + 1)
		// .setMainActivitySelection(false);
		// } else {
		// selectionMark.setChecked(true);
		// selectionMark.setVisibility(View.VISIBLE);
		// selectionCircle.setVisibility(View.VISIBLE);
		// upperBlackLayer.setVisibility(View.INVISIBLE);
		// UIShield.getPosition(position + 1)
		// .setMainActivitySelection(true);
		// }
		// }
		//
		// });
		// mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		// if (mBluetoothAdapter == null) {
		// Toast.makeText(getActivity(), "Bluetooth is not available",
		// Toast.LENGTH_LONG).show();
		// getActivity().finish();
		// return;
		// }
	}

	// private void connectDevice(Intent data) {
	// String address = data.getExtras().getString(
	// DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	// Intent intent = new Intent(getActivity(), OneSheeldService.class);
	// intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
	// getActivity().startService(intent);
	//
	// }

	private void disconnectService() {
		if (isOneSheeldServiceRunning()) {
			((MainActivity) getActivity()).stopService();
			// getActivity().stopService(
			// new Intent(getActivity(), OneSheeldService.class));
			// ((OneSheeldApplication)
			// getActivity().getApplication()).getAppFirmata()
			// .close();
			setBWStrips();
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}
	}

	private void launchShieldsOperationActivity() {
		if (!isAnyShieldsSelected()) {
			Toast.makeText(getActivity(), "Select at least 1 shield",
					Toast.LENGTH_LONG).show();
			return;
		}
		((MainActivity) getActivity()).replaceCurrentFragment(
				R.id.appTransitionsContainer, ShieldsOperations.getInstance(),
				ShieldsOperations.class.getName(), true, true);
	}

	ArduinoFirmataEventHandler sheeldsFirmataHandler = new ArduinoFirmataEventHandler() {

		@Override
		public void onError(String errorMessage) {
			UIShield.setConnected(false);
			adapter.notifyDataSetChanged();
			arduinoConnected = false;
			if (getActivity().getSupportFragmentManager()
					.getBackStackEntryCount() > 1) {
				getActivity().getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
				getActivity().getSupportFragmentManager()
						.executePendingTransactions();
			}
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}

		@Override
		public void onConnect() {
			Log.e(TAG, "- ARDUINO CONNECTED -");
			if (isOneSheeldServiceRunning()) {
				arduinoConnected = true;
				setColoredStrips();
			}
			// if (getActivity() != null)
			// ((MainActivity) getActivity())
			// .setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onClose(boolean closedManually) {
			arduinoConnected = false;
			setBWStrips();
			// if (getActivity() != null)
			// ((MainActivity) getActivity())
			// .setSupportProgressBarIndeterminateVisibility(false);
			((MainActivity) getActivity()).getOnConnectionLostHandler().connectionLost = true;
			if (((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection
					|| ((MainActivity) getActivity()).isForground)
				((MainActivity) getActivity()).getOnConnectionLostHandler()
						.sendEmptyMessage(0);
			else {
				List<Fragment> frags = getActivity()
						.getSupportFragmentManager().getFragments();
				for (Fragment frag : frags) {
					if (frag != null
							&& !frag.getClass().getName()
									.equals(SheeldsList.class.getName())
							&& !frag.getClass().getName()
									.equals(ShieldsOperations.class.getName())) {
						FragmentTransaction ft = getActivity()
								.getSupportFragmentManager().beginTransaction();
						ft.setCustomAnimations(0, 0, 0, 0);
						frag.onDestroy();
						ft.remove(frag);
						ft.commitAllowingStateLoss();
					}
				}
			}
		}
	};

	@Override
	public void onStart() {
		((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection = true;
		((OneSheeldApplication) getActivity().getApplication())
				.setArduinoFirmataEventHandler(sheeldsFirmataHandler);
		//
		// if (!mBluetoothAdapter.isEnabled()) {
		// Intent enableIntent = new Intent(
		// BluetoothAdapter.ACTION_REQUEST_ENABLE);
		// startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		//
		// }
		if (((OneSheeldApplication) getActivity().getApplication())
				.getAppFirmata() == null
				|| (((OneSheeldApplication) getActivity().getApplication())
						.getAppFirmata() != null && !((OneSheeldApplication) getActivity()
						.getApplication()).getAppFirmata().isOpen())) {
			setBWStrips();
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
			// if (((OneSheeldApplication) getActivity().getApplication())
			// .getAppPreferences().contains(
			// OneSheeldService.DEVICE_ADDRESS_KEY)) {
			// ((MainActivity) getActivity())
			// .setSupportProgressBarIndeterminateVisibility(true);
			// Intent intent = new Intent();
			// intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS,
			// ((OneSheeldApplication) getActivity().getApplication())
			// .getLastConnectedDevice());
			// connectDevice(intent);
			// }

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
		case R.id.open_bootloader_popup:
			if (!OneSheeldVersionInstallerPopup.isOpened)
				new OneSheeldVersionInstallerPopup(getActivity()).show();
			return true;
		case R.id.action_settings:
			((OneSheeldApplication) getActivity().getApplication())
					.setLastConnectedDevice(null);
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
		// UIShield.setConnected(true);
		// adapter.notifyDataSetChanged();
		// shieldsListView.setEnabled(true);
		// changeActionIconsToConnected();
	}

	private void setBWStrips() {
		// UIShield.setConnected(false);
		// adapter.notifyDataSetChanged();
		// shieldsListView.setEnabled(false);
		// changeActionIconsToDisconnected();
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
