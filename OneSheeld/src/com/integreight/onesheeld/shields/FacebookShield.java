package com.integreight.onesheeld.shields;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.helpers.AlertDialogManager;

public class FacebookShield {
	private ArduinoFirmata firmata;
	private static FacebookEventHandler eventHandler;
	private String lastPost;
	Activity activity;
	private static final byte FACEBOOK_COMMAND = (byte) 0x30;
	private static final byte UPDATE_STATUS_METHOD_ID= (byte) 0x01;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	public String getLastTweet() {
		return lastPost;
	}

	public FacebookShield(ArduinoFirmata firmata, Activity activity) {
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
				if(data.length<2)return;
				byte command = data[0];
				byte methodId=data[1];
				int n = data.length - 2;
				byte[] newArray = new byte[n];
				System.arraycopy(data, 2, newArray, 0, n);
				if (command == FACEBOOK_COMMAND) {
					String post = new String(newArray);
					lastPost = post;
				//	if (isTwitterLoggedInAlready())
//						if(methodId==UPDATE_STATUS_METHOD_ID)
//							new updateTwitterStatus().execute(tweet);

				}

			}
		});
	}

	public void setFacebookEventHandler(FacebookEventHandler eventHandler) {
		FacebookShield.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public static interface FacebookEventHandler {
		void onRecievePost(String post);

		void onFacebookLoggedIn();

		void onFacebookError(String error);
	}

	public void loginToFacebook() {

	}


	public void logoutFromFacebook() {

	}

	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFacebookLoggedInAlready() {
		// TODO Auto-generated method stub
		return false;
	}


}
