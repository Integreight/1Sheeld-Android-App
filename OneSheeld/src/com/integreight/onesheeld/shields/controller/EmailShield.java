package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.shields.controller.utils.GMailSender;
import com.integreight.onesheeld.utils.ControllerParent;

public class EmailShield extends ControllerParent<EmailShield> {

	private EmailEventHandler eventHandler;
	private static final byte EMAIL_COMMAND = (byte) 0x1E;
	private static final byte SEND_METHOD_ID = (byte) 0x01;
	private boolean isLoggedIn = false;
	private String userEmail = "";
	private String password = "";

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

		void onSuccess();
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == EMAIL_COMMAND) {
			if (frame.getFunctionId() == SEND_METHOD_ID) {
				if (isLoggedIn) {
					// send Email
					String email_send_to = frame.getArgumentAsString(0);
					String subject = frame.getArgumentAsString(1);
					String body = frame.getArgumentAsString(2);
					if (eventHandler != null)
						eventHandler.onEmailsent(email_send_to, subject);
					// sendMailUsingJavaAPI(email_send_to, subject, body);
					sendGmail(email_send_to, subject, body);
				}
			}
		}

	}

	private void sendGmail(String email_send_to, String subject, String body) {
		try {
			GMailSender sender = new GMailSender(userEmail, password,
					eventHandler);
			sender.sendMail(subject, body, userEmail, email_send_to);
		} catch (Exception e) {
			Log.d("SendMail", e.getMessage());
		}

	}

	public void setUserasLoggedIn(String userEmail, String password) {
		this.userEmail = userEmail;
		this.password = password;
		isLoggedIn = true;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}
}
