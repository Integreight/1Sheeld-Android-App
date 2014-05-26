package com.integreight.onesheeld.appFragments;

import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.integreight.onesheeld.Log;
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

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.Fields;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.ArduinoConnectivityPopup;
import com.integreight.onesheeld.FirmwareUpdatingPopup;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.OneSheeldVersionInstallerPopupTesting;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.DeviceListActivity;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.OneShieldEditText;
import com.manuelpeinado.quickreturnheader.QuickReturnHeaderHelper;

public class SheeldsList extends Fragment {
	View v;
	boolean isInflated = false;
	private ListView mListView;
	private static SheeldsList thisInstance;
	private List<UIShield> shieldsUIList;
	private ShieldsListAdapter adapter;
	private MenuItem bluetoothSearchActionButton;
	private MenuItem bluetoothDisconnectActionButton;
	private MenuItem goToShieldsOperationActionButton;
	OneShieldEditText searchBox;
	private static final String TAG = "ShieldsList";

	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 3;
	private static boolean arduinoConnected;

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
					getActivity(), R.layout.app_sheelds_list,
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
				if (getActivity() != null && searchBox != null) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				}
			}
		});
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		MainActivity.currentShieldTag = null;
		((MainActivity) getActivity()).disableMenu();
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
		getActivity().findViewById(R.id.getAvailableDevices)
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						launchShieldsOperationActivity();
					}
				});
		((ViewGroup) getActivity().findViewById(R.id.getAvailableDevices))
				.getChildAt(1).setBackgroundResource(
						R.drawable.shields_list_shields_operation_button);
		((ViewGroup) getActivity().findViewById(R.id.cancelConnection))
				.getChildAt(1).setBackgroundResource(
						R.drawable.bluetooth_disconnect_button);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				getActivity().findViewById(R.id.cancelConnection)
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								if (getActivity().getSupportFragmentManager()
										.getBackStackEntryCount() > 1) {
									getActivity().getSupportFragmentManager()
											.popBackStack();
									getActivity().getSupportFragmentManager()
											.executePendingTransactions();
								}
								((MainActivity) getActivity()).stopService();
								if (!ArduinoConnectivityPopup.isOpened) {
									ArduinoConnectivityPopup.isOpened = true;
									new ArduinoConnectivityPopup(getActivity())
											.show();
								}
							}
						});
			}
		}, 500);
		((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection = true;
		((OneSheeldApplication) getActivity().getApplication())
				.setArduinoFirmataEventHandler(sheeldsFirmataHandler);
		if (((OneSheeldApplication) getActivity().getApplication())
				.getAppFirmata() == null
				|| (((OneSheeldApplication) getActivity().getApplication())
						.getAppFirmata() != null && !((OneSheeldApplication) getActivity()
						.getApplication()).getAppFirmata().isOpen())) {
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}
		Crashlytics.setString("Current View", "Shields List");
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// setRetainInstance(true);
		if (isInflated)
			initView();
		super.onActivityCreated(savedInstanceState);
	}

	int lastTranslate = 0;

	private void initView() {
		// mListView.addHeaderView(mHeader);
		shieldsUIList = UIShield.valuesFiltered();
		adapter = new ShieldsListAdapter(getActivity());
		mListView.setAdapter(adapter);
		// mListView.setSelection(1);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		mListView.setDrawingCacheEnabled(true);
		searchBox = (OneShieldEditText) v.findViewById(R.id.searchArea);
		searchBox.setAdapter(adapter);
		searchBox.setDropDownHeight(0);
		v.findViewById(R.id.selectAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
								0);
						for (UIShield shield : UIShield.valuesFiltered()) {
							UIShield.valueOf(shield.name())
									.setMainActivitySelection(true);
						}
						searchBox.setText("");
						adapter.selectAll();
					}
				});
		v.findViewById(R.id.reset).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
								0);
						for (UIShield shield : UIShield.valuesFiltered()) {
							UIShield.valueOf(shield.name())
									.setMainActivitySelection(false);
						}
						searchBox.setText("");
						adapter.reset();
					}
				});
		v.findViewById(R.id.clearBox).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
								0);
						searchBox.setText("");
					}
				});
		mListView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				return false;
			}
		});
	}

	private void disconnectService() {
		if (isOneSheeldServiceRunning()) {
			((MainActivity) getActivity()).stopService();
			if (!ArduinoConnectivityPopup.isOpened)
				new ArduinoConnectivityPopup(getActivity()).show();
		}
	}

	private void launchShieldsOperationActivity() {
		if (!isAnyShieldsSelected()) {
			Toast.makeText(getActivity(), "Select at least 1 shield",
					Toast.LENGTH_LONG).show();
			return;
		} else {
			((MainActivity) getActivity()).replaceCurrentFragment(
					R.id.appTransitionsContainer,
					ShieldsOperations.getInstance(),
					ShieldsOperations.class.getName(), true, true);
			getActivity().findViewById(R.id.getAvailableDevices)
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

						}
					});
		}
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
				((OneSheeldApplication) getActivity().getApplication())
						.getGaTracker().set(Fields.SESSION_CONTROL, "start");
				arduinoConnected = true;
				if (adapter != null)
					adapter.applyToControllerTable();
			}
		}

		@Override
		public void onClose(boolean closedManually) {
			arduinoConnected = false;
			if (getActivity() != null) {
				((OneSheeldApplication) getActivity().getApplication())
						.getGaTracker().set(Fields.SESSION_CONTROL, "end");
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
								&& !frag.getClass()
										.getName()
										.equals(ShieldsOperations.class
												.getName())) {
							FragmentTransaction ft = getActivity()
									.getSupportFragmentManager()
									.beginTransaction();
							ft.setCustomAnimations(0, 0, 0, 0);
							frag.onDestroy();
							ft.remove(frag);
							ft.commitAllowingStateLoss();
						}
					}
				}
				Enumeration<String> enumKey = ((OneSheeldApplication) getActivity()
						.getApplication()).getRunningShields().keys();
				while (enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					((OneSheeldApplication) getActivity().getApplication())
							.getRunningShields().get(key).resetThis();
					((OneSheeldApplication) getActivity().getApplication())
							.getRunningShields().remove(key);
				}
			}
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
			if (!OneSheeldVersionInstallerPopupTesting.isOpened)
				new FirmwareUpdatingPopup((MainActivity) getActivity(), false)
						.show();
			return true;
		case R.id.open_bootloader_popup_china:
			if (!OneSheeldVersionInstallerPopupTesting.isOpened)
				new FirmwareUpdatingPopup((MainActivity) getActivity(), true)
						.show();
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

	private boolean isAnyShieldsSelected() {
		int i = 0;
		OneSheeldApplication app = (OneSheeldApplication) getActivity()
				.getApplication();
		// app.setRunningSheelds(new Hashtable<String, ControllerParent<?>>());
		for (UIShield shield : shieldsUIList) {
			if (shield.isMainActivitySelection()
					&& shield.getShieldType() != null) {
				if (app.getRunningShields().get(shield.name()) == null) {
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
							shield.name());
				}
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
