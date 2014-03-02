package com.integreight.onesheeld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Window;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.utils.AppSlidingLeftMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

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
			if (appSlidingMenu.isOpen()) {
				appSlidingMenu.closePane();
			} else {
				if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
					getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
					getSupportFragmentManager().executePendingTransactions();
				} else
					finish();
			}
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
			if (addToBackStack) {
				// ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				if (animate)
					ft.setCustomAnimations(R.anim.slide_out_left,
							R.anim.slide_in_right, 0, 0);
			}
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
}
