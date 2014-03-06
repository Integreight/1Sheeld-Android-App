package com.integreight.onesheeld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;

public class MainActivity extends FragmentActivity {
	// private final String TAG = "MainActivity";
	// private boolean isBoundService = false;
	private AppSlidingLeftMenu appSlidingMenu;

	public OneSheeldApplication getThisApplication() {
		return (OneSheeldApplication) getApplication();
	}

	// private ArduinoFirmataEventHandler arduinoEventHandler = new
	// ArduinoFirmataEventHandler() {
	//
	// @Override
	// public void onError(String errorMessage) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onConnect() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onClose(boolean closedManually) {
	// // TODO Auto-generated method stub
	//
	// }
	// };
	//
	// public void setArduinoFirmataHandler(ArduinoFirmataEventHandler handler)
	// {
	// this.arduinoEventHandler = handler;
	// }

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.one_sheeld_main);
		// set the Behind View
		// setBehindContentView(R.layout.menu_frame);
		replaceCurrentFragment(R.id.appTransitionsContainer,
				SheeldsList.getInstance(), "base", true, false);
		findViewById(R.id.cancelConnection).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (getSupportFragmentManager()
								.getBackStackEntryCount() > 1) {
							getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
							getSupportFragmentManager()
									.executePendingTransactions();
						}
						stopService();
						new ArduinoConnectivityPopup(MainActivity.this).show();
					}
				});
		resetSlidingMenu();
	}

	@Override
	public void onBackPressed() {
		resetSlidingMenu();
		if (!ArduinoConnectivityPopup.isOpened) {
			MultiDirectionSlidingDrawer pinsView = (MultiDirectionSlidingDrawer) findViewById(R.id.pinsViewSlidingView);
			if (pinsView == null || (pinsView != null && !pinsView.isOpened())) {
				if (appSlidingMenu.isOpen()) {
					appSlidingMenu.closePane();
				} else {
					if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
						getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
						getSupportFragmentManager()
								.executePendingTransactions();
					} else
						finish();
				}
			} else
				pinsView.animateOpen();
		} else
			finish();
	}

	public void replaceCurrentFragment(int container, Fragment targetFragment,
			String tag, boolean addToBackStack, boolean animate) {
		String backStateName = targetFragment.getClass().getName();
		String fragmentTag = backStateName;

		FragmentManager manager = getSupportFragmentManager();
		boolean fragmentPopped = manager
				.popBackStackImmediate(backStateName, 0);

		if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { // fragment
			// not
			// in
			// back
			// stack,
			// create
			// it.
			FragmentTransaction ft = manager.beginTransaction();
			if (animate)
				ft.setCustomAnimations(R.anim.slide_out_right,
						R.anim.slide_in_left, R.anim.slide_out_left,
						R.anim.slide_in_right);
			ft.replace(container, targetFragment, fragmentTag);
			if (addToBackStack) {
				ft.addToBackStack(backStateName);
			}
			ft.commit();
		}
	}

	public void stopService() {
		this.stopService(new Intent(this, OneSheeldService.class));

	}

	@Override
	protected void onDestroy() {
		// isBoundService = OneSheeldService.isBound;
		// if (isMyServiceRunning())
		ArduinoConnectivityPopup.isOpened = false;
		stopService();
		// isBoundService = false;
		super.onDestroy();
	}

	//
	// private boolean isMyServiceRunning() {
	// ActivityManager manager = (ActivityManager)
	// getSystemService(Context.ACTIVITY_SERVICE);
	// for (RunningServiceInfo service : manager
	// .getRunningServices(Integer.MAX_VALUE)) {
	// if (OneSheeldApplication.class.getName().equals(
	// service.service.getClassName())) {
	// return true;
	// }
	// }
	// return false;
	// }
	public void openMenu() {
		resetSlidingMenu();
		appSlidingMenu.openPane();
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
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SheeldsList.REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			// if (resultCode == Activity.RESULT_OK) {
			// ((MainActivity) getActivity())
			// .setSupportProgressBarIndeterminateVisibility(true);
			// // connectDevice(data);
			// }
			break;
		case SheeldsList.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				ArduinoConnectivityPopup.isOpened = false;
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
