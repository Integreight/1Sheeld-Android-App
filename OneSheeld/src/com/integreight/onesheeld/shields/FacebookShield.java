package com.integreight.onesheeld.shields;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class FacebookShield {
	private ArduinoFirmata firmata;
	private static FacebookEventHandler eventHandler;
	private String lastPost;
	private Activity activity;
	private Fragment fragment;
	private static final byte FACEBOOK_COMMAND = (byte) 0x30;
	private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;

	static final String PREF_KEY_FACEBOOK_USERNAME = "FacebookName";

	private static SharedPreferences mSharedPreferences;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	public String getLastTweet() {
		return lastPost;
	}

	public FacebookShield(ArduinoFirmata firmata, Activity activity,
			Fragment fragment, Bundle savedInstanceState) {
		this.firmata = firmata;
		this.activity = activity;
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(activity, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(activity);
            }
            Session.setActiveSession(session);
        }
        
        session.addCallback(statusCallback);
		this.fragment = fragment;
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
				if (command == FACEBOOK_COMMAND) {
					String post = new String(newArray);
					lastPost = post;
					// if (isTwitterLoggedInAlready())
					// if(methodId==UPDATE_STATUS_METHOD_ID)
					// new updateTwitterStatus().execute(tweet);

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
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(fragment)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(activity, fragment, true, statusCallback);
		}
	}

	public void logoutFromFacebook() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_FACEBOOK_USERNAME);
		e.commit();
	}

	public String getUsername() {
		// TODO Auto-generated method stub
		return mSharedPreferences.getString(PREF_KEY_FACEBOOK_USERNAME, "");
	}

	public boolean isFacebookLoggedInAlready() {
		// TODO Auto-generated method stub
		return Session.getActiveSession().isOpened();
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if(exception!=null&&eventHandler!=null)eventHandler.onFacebookError(exception.getMessage());
			if (session.isOpened()) {

				// make request to the /me API
				Request.newMeRequest(session, new Request.GraphUserCallback() {

					// callback after Graph API response with user object
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {


							Editor e = mSharedPreferences.edit();
							e.putString(PREF_KEY_FACEBOOK_USERNAME, user.getName());
							e.commit();
							if(eventHandler!=null)eventHandler.onFacebookLoggedIn();
						}
					}
				}).executeAsync();

			}
		}
	}

}
