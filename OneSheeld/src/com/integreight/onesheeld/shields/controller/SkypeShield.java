package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.utils.ControllerParent;

public class SkypeShield extends ControllerParent<SkypeShield> {

	private SkypeEventHandler eventHandler;
	private static final byte SKYPE_COMMAND = (byte) 0x1F;
	private static final byte CALL_METHOD_ID = (byte) 0x01;
	private static final byte VIDEO_METHOD_ID = (byte) 0x02;
	private static final byte CHAT_METHOD_ID = (byte) 0x03;

	public SkypeShield() {
		super();
	}
	@Override
	public ControllerParent<SkypeShield> setTag(String tag) {
		getApplication().getAppFirmata().initUart();
		return super.setTag(tag);
	}

	public SkypeShield(Activity activity, String tag) {
		super(activity, tag);
		getApplication().getAppFirmata().initUart();

	}

	public void setSkypeEventHandler(SkypeEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface SkypeEventHandler {
		void onCall(String user);

		void onVideoCall(String user);

		void onChat(String user);

		void onError(String error);

		void onSkypeClientNotInstalled(String popMessage);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == SKYPE_COMMAND) {
			/*
			 * if (frame.getFunctionId() == CALL_METHOD_ID) { String user =
			 * frame.getArgumentAsString(0); if (eventHandler != null)
			 * eventHandler.onCall(user); }
			 */
			String userId = frame.getArgumentAsString(0);
			Log.d("Skype_User_ID ", userId);

			switch (frame.getFunctionId()) {
			case CALL_METHOD_ID:
				callSkypeID(userId);
				break;
			case VIDEO_METHOD_ID:
				videoCallSkypeID(userId);
				break;
			case CHAT_METHOD_ID:
				chatSkypeID(userId);
				break;

			default:
				break;
			}
		}

	}

	private void callSkypeID(String userId) {
		if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
			Log.d("Skype Client Installed", "Yes");
			// Create the Intent from our Skype URI
			Uri skypeUri = Uri.parse("skype:" + userId + "?call");
			Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

			// Restrict the Intent to being handled by the Skype for
			// Android client only
			myIntent.setComponent(new ComponentName("com.skype.raider",
					"com.skype.raider.Main"));
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Initiate the Intent. It should never fail since we've
			// already established the
			// presence of its handler (although there is an extremely
			// minute window where that
			// handler can go away...)
			getActivity().startActivity(myIntent);
			if (eventHandler != null)
				eventHandler.onCall(userId);

		} else {
			Log.d("Skype Client Installed", "No");
			if (eventHandler != null)
				eventHandler
						.onSkypeClientNotInstalled("Skype Client is not Installed");

		}

	}

	private void videoCallSkypeID(String userId) {
		if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
			Log.d("Skype Client Installed", "Yes");
			// Create the Intent from our Skype URI
			Uri skypeUri = Uri.parse("skype:" + userId + "?call&video=true");
			Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

			// Restrict the Intent to being handled by the Skype for
			// Android client only
			myIntent.setComponent(new ComponentName("com.skype.raider",
					"com.skype.raider.Main"));
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Initiate the Intent. It should never fail since we've
			// already established the
			// presence of its handler (although there is an extremely
			// minute window where that
			// handler can go away...)
			getActivity().startActivity(myIntent);
			if (eventHandler != null)
				eventHandler.onVideoCall(userId);

		} else {
			Log.d("Skype Client Installed", "No");
			if (eventHandler != null)
				eventHandler
						.onSkypeClientNotInstalled("Skype Client is not Installed");

		}

	}

	private void chatSkypeID(String userId) {
		if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
			Log.d("Skype Client Installed", "Yes");
			// Create the Intent from our Skype URI
			Uri skypeUri = Uri.parse("skype:" + userId + "?chat");
			Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

			// Restrict the Intent to being handled by the Skype for
			// Android client only
			myIntent.setComponent(new ComponentName("com.skype.raider",
					"com.skype.raider.Main"));
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Initiate the Intent. It should never fail since we've
			// already established the
			// presence of its handler (although there is an extremely
			// minute window where that
			// handler can go away...)
			getActivity().startActivity(myIntent);
			if (eventHandler != null)
				eventHandler.onChat(userId);

		} else {
			Log.d("Skype Client Installed", "No");
			if (eventHandler != null)
				eventHandler
						.onSkypeClientNotInstalled("Skype Client is not Installed");

		}

	}

	public boolean isSkypeClientInstalled(Context myContext) {
		PackageManager myPackageMgr = myContext.getPackageManager();
		try {
			myPackageMgr.getPackageInfo("com.skype.raider",
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			return (false);
		}
		return (true);
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
