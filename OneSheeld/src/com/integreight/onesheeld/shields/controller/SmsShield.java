package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.SmsListener;
import com.integreight.onesheeld.shields.controller.utils.SmsListener.SmsReceiveEventHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class SmsShield extends ControllerParent<SmsShield> {
	private SmsEventHandler eventHandler;
	private String lastSmsText;
	private String lastSmsNumber;
	private static final byte SEND_SMS_METHOD_ID = (byte) 0x01;
	private SmsListener smsListener;
	private ShieldFrame frame;

	public String getLastSmsText() {
		return lastSmsText;
	}

	public String getLastSmsNumber() {
		return lastSmsNumber;
	}

	public SmsShield() {
		super();
	}

	@Override
	public ControllerParent<SmsShield> setTag(String tag) {
		smsListener = new SmsListener();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		if (smsReceiveEventHandler != null)
			smsListener.setSmsReceiveEventHandler(smsReceiveEventHandler);
		getActivity().registerReceiver(smsListener, filter);
		return super.setTag(tag);
	}

	public SmsShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void onUartReceive(byte[] data) {
		// if (data.length < 2)
		// return;
		// byte command = data[0];
		// byte methodId = data[1];
		// int n = data.length - 2;
		// byte[] newArray = new byte[n];
		// System.arraycopy(data, 2, newArray, 0, n);

		super.onUartReceive(data);
	}

	protected void sendSms(String smsNumber, String smsText) {

		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(smsNumber, null, smsText, null, null);
			if (eventHandler != null)
				eventHandler.onSmsSent(smsNumber, smsText);
		} catch (Exception e) {
			if (eventHandler != null)
				eventHandler.onSmsFail(e.getMessage());

			e.printStackTrace();
		}
		CommitInstanceTotable();

	}

	public void setSmsEventHandler(SmsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public interface SmsEventHandler {
		void onSmsSent(String smsNumber, String smsText);

		void onSmsFail(String error);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == UIShield.SMS_SHIELD.getId()) {
			String smsNumber = frame.getArgumentAsString(0);
			String smsText = frame.getArgumentAsString(1);
			lastSmsText = smsText;
			if (frame.getFunctionId() == SEND_SMS_METHOD_ID) {
				sendSms(smsNumber, smsText);
			}

		}

	}

	private SmsReceiveEventHandler smsReceiveEventHandler = new SmsReceiveEventHandler() {

		@Override
		public void onSmsReceiveSuccess(String mobile_num, String sms_body) {
			// send frame contain SMS body..
			Log.d("SMS::Controller::onSmsReceiveSuccess", sms_body);
			frame = new ShieldFrame(UIShield.SMS_SHIELD.getId(), (byte) 0x01);
			frame.addStringArgument(mobile_num);
			frame.addStringArgument(sms_body);

			Log.d("Fram", frame.getArgumentAsString(1));
			sendShieldFrame(frame);
		}

		@Override
		public void onSmsReceiveFailed() {
			Log.d("SMS::Controller::onSmsReceiveFailed",
					"Failed to receive SMS !");
		}
	};

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		frame = null;
		smsListener = null;

		try {
			getActivity().unregisterReceiver(smsListener);
		} catch (Exception e) {
			// TODO: handle exception

		}
	}

	public void sendSmsToArduino() {
		smsListener.sendSMS();
	}
}
