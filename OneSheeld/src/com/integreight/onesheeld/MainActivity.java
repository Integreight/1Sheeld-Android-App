package com.integreight.onesheeld;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoVersionQueryHandler;
import com.integreight.onesheeld.ArduinoConnectivityPopup.onConnectedToBluetooth;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.shields.controller.utils.GmailSinginPopup;
import com.integreight.onesheeld.utils.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.ValidationPopup;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;

public class MainActivity extends FragmentActivity {
	// private final String TAG = "MainActivity";
	// private boolean isBoundService = false;
	private AppSlidingLeftMenu appSlidingMenu;
	public boolean isForground = false;
	private onConnectedToBluetooth onConnectToBlueTooth = null;

	public OneSheeldApplication getThisApplication() {
		return (OneSheeldApplication) getApplication();
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.one_sheeld_main);
		initLooperThread();
		// set the Behind View
		// setBehindContentView(R.layout.menu_frame);
		replaceCurrentFragment(R.id.appTransitionsContainer,
				SheeldsList.getInstance(), "base", true, false);
		resetSlidingMenu();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread arg0, final Throwable arg1) {
				arg1.printStackTrace();
				ArduinoConnectivityPopup.isOpened = false;
				// stopService(new Intent(getApplicationContext(),
				// OneSheeldService.class));
				moveTaskToBack(true);
				if (((OneSheeldApplication) getApplication()).getAppFirmata() != null) {
					while (!((OneSheeldApplication) getApplication())
							.getAppFirmata().close())
						;
				}
				stopService();
				new Thread(new Runnable() {

					@Override
					public void run() {
						StringWriter sw = new StringWriter();
						arg1.printStackTrace(new PrintWriter(sw));
						String exceptionAsString = sw.toString();
						GmailSinginPopup.sendReportMail(
								"ahmed.ebnsaad@gmail.com",
								"egydroid@gmail.com", arg1.getMessage(),
								exceptionAsString != null ? exceptionAsString
										: "", "knginekehna");
						Intent in = new Intent(getIntent());
						PendingIntent intent = PendingIntent
								.getActivity(getBaseContext(), 0, in,
										getIntent().getFlags());

						AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
						mgr.set(AlarmManager.RTC,
								System.currentTimeMillis() + 1000, intent);
						System.exit(0);
					}
				}).start();
			}
		});
		if (getThisApplication().getAppFirmata() != null) {
			getThisApplication().getAppFirmata().addVersionQueryHandler(
					versionChangingHandler);
		}
		// ValidationPopup popub = new ValidationPopup(MainActivity.this,
		// "Firmware Upgrading", "There's a new version for your 1Sheeld",
		// new ValidationPopup.ValidationAction("Now",
		// new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// new FirmwareUpdatingPopup(MainActivity.this)
		// .show();
		// }
		// }, true), new ValidationPopup.ValidationAction(
		// "Not Now", new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		//
		// }
		// }, true));
		// popub.show();
	}

	private Thread looperThread;
	public Handler backgroundThreadHandler;
	private Looper backgroundHandlerLooper;

	private void stopLooperThread() {
		if (looperThread != null && looperThread.isAlive()) {
			looperThread.interrupt();
			backgroundHandlerLooper.quit();
			looperThread = null;
		}
	}

	private void initLooperThread() {
		stopLooperThread();
		looperThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				backgroundHandlerLooper = Looper.myLooper();
				backgroundThreadHandler = new Handler();
				Looper.loop();
			}
		});
		looperThread.start();
	}

	Handler versionHandling = new Handler();
	ArduinoVersionQueryHandler versionChangingHandler = new ArduinoVersionQueryHandler() {
		ValidationPopup popub;

		@Override
		public void onVersionReceived(final int minorVersion,
				final int majorVersion) {
			versionHandling.post(new Runnable() {

				@Override
				public void run() {
					Log.d("Onesheeld", minorVersion + "     " + majorVersion);
					if (majorVersion == getThisApplication().getMajorVersion()
							&& minorVersion != getThisApplication()
									.getMinorVersion()) {
						popub = new ValidationPopup(MainActivity.this,
								"Firmware Upgrading",
								"There's a new version for your 1Sheeld",
								new ValidationPopup.ValidationAction("Now",
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												new FirmwareUpdatingPopup(
														MainActivity.this,
														false).show();
											}
										}, true),
								new ValidationPopup.ValidationAction("Not Now",
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												// stub

											}
										}, true));
						// if (!isFinishing())
						// popub.show();
					} else if (majorVersion != getThisApplication()
							.getMajorVersion()
							&& minorVersion != getThisApplication()
									.getMinorVersion()) {
						popub = new ValidationPopup(
								MainActivity.this,
								"Firmware Upgrading",
								"Your 1Sheeld is not valid yet, There's a new version ready!",
								new ValidationPopup.ValidationAction("Start",
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												new FirmwareUpdatingPopup(
														MainActivity.this,
														false).show();
											}
										}, true));
						// if (!isFinishing())
						// popub.show();
					}
				}
			});

		}
	};

	@Override
	protected void onResume() {
		findViewById(R.id.cancelConnection).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (getSupportFragmentManager()
								.getBackStackEntryCount() > 1) {
							getSupportFragmentManager().popBackStack();
							getSupportFragmentManager()
									.executePendingTransactions();
						}
						stopService();
						if (!ArduinoConnectivityPopup.isOpened) {
							ArduinoConnectivityPopup.isOpened = true;
							new ArduinoConnectivityPopup(MainActivity.this)
									.show();
						}
					}
				});
		super.onResume();
	}

	private BackOnconnectionLostHandler backOnConnectionLostHandler;

	public BackOnconnectionLostHandler getOnConnectionLostHandler() {
		if (backOnConnectionLostHandler == null) {
			backOnConnectionLostHandler = new BackOnconnectionLostHandler() {

				@Override
				public void handleMessage(Message msg) {
					if (connectionLost) {
						runOnUiThread(new Runnable() {
							public void run() {
								if (!ArduinoConnectivityPopup.isOpened)
									new ArduinoConnectivityPopup(
											MainActivity.this).show();
							}
						});
						if (getSupportFragmentManager()
								.getBackStackEntryCount() > 1) {
							getSupportFragmentManager().beginTransaction()
									.setCustomAnimations(0, 0, 0, 0).commit();
							getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
							getSupportFragmentManager()
									.executePendingTransactions();
						}
					}
					connectionLost = false;
					super.handleMessage(msg);
				}
			};
		}
		return backOnConnectionLostHandler;
	}

	public class BackOnconnectionLostHandler extends Handler {
		public boolean canInvokeOnCloseConnection = true,
				connectionLost = false;
	}

	@Override
	public void onBackPressed() {
		resetSlidingMenu();
		MultiDirectionSlidingDrawer pinsView = (MultiDirectionSlidingDrawer) findViewById(R.id.pinsViewSlidingView);
		MultiDirectionSlidingDrawer settingsView = (MultiDirectionSlidingDrawer) findViewById(R.id.settingsSlidingView);
		if ((pinsView == null || (pinsView != null && !pinsView.isOpened()))
				&& (settingsView == null || (settingsView != null && !settingsView
						.isOpened()))) {
			if (appSlidingMenu.isOpen()) {
				appSlidingMenu.closePane();
			} else {
				if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
					getSupportFragmentManager().popBackStack();// ("operations",FragmentManager.POP_BACK_STACK_INCLUSIVE);
					getSupportFragmentManager().executePendingTransactions();
				} else
					moveTaskToBack(true);
			}
		} else {
			if (pinsView.isOpened())
				pinsView.animateOpen();
			else if (settingsView.isOpened())
				settingsView.animateOpen();
		}
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
		stopLooperThread();
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

	public void setOnConnectToBluetooth(onConnectedToBluetooth listener) {
		this.onConnectToBlueTooth = listener;
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
				// ArduinoConnectivityPopup.isOpened = false;
				// finish();
			} else {
				if (onConnectToBlueTooth != null
						&& ArduinoConnectivityPopup.isOpened)
					onConnectToBlueTooth.onConnect();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart() {
		isForground = true;
		super.onStart();
	}

	@Override
	protected void onStop() {
		isForground = false;
		super.onStop();
	}

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
