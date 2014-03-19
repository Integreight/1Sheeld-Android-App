package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.utils.ControllerParent;

public class EmptyShield extends ControllerParent<EmptyShield> {

	public EmptyShield() {
		super();
	}

	public EmptyShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
