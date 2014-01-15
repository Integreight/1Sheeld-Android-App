package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.telephony.SmsManager;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class SmsShield {
	private ArduinoFirmata firmata;
	private SmsEventHandler eventHandler;
	private String lastSmsText;
	private String lastSmsNumber;
	private Activity activity;
	private static final byte SMS_COMMAND = (byte) 0x36;
	private static final byte SEND_SMS_METHOD_ID = (byte) 0x01;
	private static final String separator="##";

	public String getLastSmsText() {
		return lastSmsText;
	}

	public String getLastSmsNumber() {
		return lastSmsNumber;
	}

	public SmsShield(ArduinoFirmata firmata, Activity activity) {
		this.firmata = firmata;
		this.activity = activity;
	}

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub
				if (data.length < 2)
					return;
				byte command = data[0];
				byte methodId = data[1];
				int n = data.length - 2;
				byte[] newArray = new byte[n];
				System.arraycopy(data, 2, newArray, 0, n);
				if (command == SMS_COMMAND) {
					String smsRaw = new String(newArray);
					String smsNumber=smsRaw.substring(0, smsRaw.indexOf(separator));
					String smsText=smsRaw.substring(smsRaw.indexOf(separator)+separator.length());
					lastSmsText = smsRaw;
					if (methodId == SEND_SMS_METHOD_ID) {
						sendSms(smsNumber, smsText);

					}

				}

			}
		});
	}

	protected void sendSms(String smsNumber, String smsText) {

		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(smsNumber, null, smsText, null, null);
			eventHandler.onSmsSent(smsText, smsText);
		} catch (Exception e) {
			eventHandler.onSmsFail(e.getMessage());

			e.printStackTrace();
		}

	}

	public void setSmsEventHandler(SmsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public interface SmsEventHandler {
		void onSmsSent(String smsNumber, String smsText);

		void onSmsFail(String error);
	}

}
