package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Intent;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.utils.ControllerParent;

public class EmailShield extends ControllerParent<EmailShield> {

	private EmailEventHandler eventHandler;
	private static final byte EMAIL_COMMAND = (byte) 0x1E;
	private static final byte SEND_METHOD_ID = (byte) 0x01;
	private boolean isLoggedIn = false;

	public EmailShield() {
		super();
	}

	@Override
	public ControllerParent<EmailShield> setTag(String tag) {
		return super.setTag(tag);
	}

	public EmailShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public void setEmailEventHandler(EmailEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface EmailEventHandler {
		void onEmailsent(String email_send_to, String subject);

		void onSendingError(String error);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == EMAIL_COMMAND) {

			if (isLoggedIn) {
				// send Email
				String email_send_to = frame.getArgumentAsString(0);
				String subject = frame.getArgumentAsString(1);
				String body = frame.getArgumentAsString(2);
				eventHandler.onEmailsent(email_send_to, subject);
				// sendMail(email_send_to, subject, body);
			}
		}

	}

	public void setUserasLoggedIn(String userEmail, String password) {
		isLoggedIn = true;
	}

	private void sendMail(String email, String subject, String body) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		emailIntent.setType("text/plain");
		try {
			getActivity().startActivity(
					Intent.createChooser(emailIntent, "com.google").addFlags(
							Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (android.content.ActivityNotFoundException ex) {
			Log.d("Email Sheeld:: Send Email()",
					"There are no email clients installed");
		}
		Log.d("Email Sheeld:: Send Email()", email);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
