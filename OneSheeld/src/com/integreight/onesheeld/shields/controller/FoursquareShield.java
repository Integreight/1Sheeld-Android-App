package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.utils.ControllerParent;

public class FoursquareShield extends ControllerParent<FoursquareShield> {

	private FoursquareEventHandler eventHandler;

	public FoursquareShield() {
		super();
	}

	@Override
	public ControllerParent<FoursquareShield> setTag(String tag) {
		getApplication().getAppFirmata().initUart();
		return super.setTag(tag);
	}

	public FoursquareShield(Activity activity, String tag) {
		super(activity, tag);
		getApplication().getAppFirmata().initUart();

	}

	public void setFoursquareEventHandler(FoursquareEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		getApplication().getAppFirmata().initUart();
		CommitInstanceTotable();
	}

	public static interface FoursquareEventHandler {
		void onPlaceCheckin();

		void onForsquareLoggedIn();

		void onForsquareLogout();

		void onForsquareError();

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

}
