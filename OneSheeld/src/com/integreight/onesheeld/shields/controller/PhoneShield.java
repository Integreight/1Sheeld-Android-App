package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.PhoneCallStateListener;
import com.integreight.onesheeld.shields.controller.utils.PhoneCallStateListener.PhoneRingingEventHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class PhoneShield extends ControllerParent<PhoneShield> {
	// private PhoneEventHandler eventHandler;
	private static final byte CALL_METHOD_ID = (byte) 0x01;
	private PhoneCallStateListener phoneListener;
	private TelephonyManager telephonyManager;
	private ShieldFrame frame;

	public PhoneShield() {
	}

	public PhoneShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<PhoneShield> setTag(String tag) {
		phoneListener = new PhoneCallStateListener();
		phoneListener.setPhoneRingingEventHandler(phoneRingingEventHandler);
		telephonyManager = (TelephonyManager) getApplication()
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		return super.setTag(tag);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.PHONE_SHIELD.getId()) {
			String phone_number = frame.getArgumentAsString(0);

			switch (frame.getFunctionId()) {
			case CALL_METHOD_ID:
				call(phone_number);
				break;
			default:
				break;
			}
		}

	}

	public void setPhoneEventHandler(PhoneEventHandler eventHandler) {
		// this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface PhoneEventHandler {
		void OnCall(String phone_number);

		void isRinging(boolean isRinging);
	}

	private void call(String phoneNumber) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + phoneNumber));
		callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(callIntent);
		// Set Handlers to update UI..
	}

	private PhoneRingingEventHandler phoneRingingEventHandler = new PhoneRingingEventHandler() {

		@Override
		public void sendIncomingNumber(String phoneNumber) {
			// send frame contain Incoming Number..
			Log.d("Phone::Controller::SendIncomingNum", phoneNumber);
			frame = new ShieldFrame(UIShield.PHONE_SHIELD.getId(), (byte) 0x02);
			frame.addStringArgument(phoneNumber);
			sendShieldFrame(frame);
		}

		@Override
		public void isPhoneRinging(boolean isRinging) {
			// send frame with Ringing state..
			Log.d("Phone::Controller::isPhoneRinging", isRinging + "");
			frame = new ShieldFrame(UIShield.PHONE_SHIELD.getId(), (byte) 0x01);
			if (isRinging) {
				frame.addByteArgument((byte) 1);
			} else {
				frame.addByteArgument((byte) 0);
			}
			sendShieldFrame(frame);
		}
	};

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		frame = null;
		if (phoneListener != null && telephonyManager != null)
			telephonyManager.listen(phoneListener,
					PhoneStateListener.LISTEN_NONE);
	}

}
