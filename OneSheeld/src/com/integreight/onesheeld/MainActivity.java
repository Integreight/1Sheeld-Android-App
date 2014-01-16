package com.integreight.onesheeld;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.appFragments.SheeldsList;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.services.OneSheeldService.OneSheeldBinder;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends SherlockFragmentActivity {
	private final String TAG = "MainActivity";

	public OneSheeldApplication getThisApplication() {
		return (OneSheeldApplication) getApplication();
	}

	private OneSheeldService _1SheeldService;

	private ArduinoFirmataEventHandler arduinoEventHandler = new ArduinoFirmataEventHandler() {

		@Override
		public void onError(String errorMessage) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnect() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onClose(boolean closedManually) {
			// TODO Auto-generated method stub

		}
	};

	public void setArduinoFirmataHandler(ArduinoFirmataEventHandler handler) {
		this.arduinoEventHandler = handler;
		bindFirmataService();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			OneSheeldBinder binder = (OneSheeldBinder) service;
			_1SheeldService = binder.getService();

			getThisApplication().setBoundService(true);
			getThisApplication().setAppFirmata(_1SheeldService.getFirmata());
			getThisApplication().getAppFirmata().addEventHandler(
					arduinoEventHandler);
			for (OneSheeldServiceHandler serviceHandler : ((OneSheeldApplication) getApplication())
					.getServiceEventHandlers()) {
				serviceHandler.onSuccess(getThisApplication().getAppFirmata());
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			getThisApplication().setBoundService(false);
			for (OneSheeldServiceHandler serviceHandler : ((OneSheeldApplication) getApplication())
					.getServiceEventHandlers()) {
				serviceHandler.onFailure();
			}
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.one_sheeld_main);
		replaceCurrentFragment(SheeldsList.getInstance());
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else
			finish();
		super.onBackPressed();
	}

	public void replaceCurrentFragment(Fragment targetFragment) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.appTransitionsContainer, targetFragment);
		transaction.commit();
	}

	private void bindFirmataService() {
		if (!getThisApplication().isBoundService()) {
			Intent intent = new Intent(this, OneSheeldService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	private void unBindFirmataService() {
		// TODO Auto-generated method stub
		if (getThisApplication().isBoundService())
			this.unbindService(mConnection);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		unBindFirmataService();
		super.onDestroy();
	}

}
