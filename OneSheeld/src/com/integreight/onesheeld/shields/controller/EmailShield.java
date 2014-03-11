package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.os.AsyncTask;

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
	private static String message_body = "";
	private static String message_reciption = "";
	private static String message_subject = "";

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

		void onSendingAuthError(String error);

		void onSuccess();

		void onEmailnotSent(String message_not_sent);

		void onLoginSuccess(String userName, String password);
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
		message_body = body;
		message_reciption = email_send_to;
		message_subject = subject;
		new sendGmailinBackground().execute();

	}

	public class sendGmailinBackground extends AsyncTask<Void, Void, Integer> {
		GMailSender sender = new GMailSender(userEmail, password);
		int result;

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				result = sender.sendMail(message_subject, message_body,
						userEmail, message_reciption);
			} catch (Exception e) {
				Log.d("SendMail", e.getMessage());
			}
			return result;
		}
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch (result) {
			case 0:
				eventHandler.onSuccess();
				break;
			case 1:
				eventHandler.onEmailnotSent("message could not be sent to the recipient");
				break;
			case 2:
				eventHandler.onEmailnotSent("message could not be sent to the recipient ");
				break;
			case 3:
				eventHandler.onEmailnotSent("message could not be sent to the recipient ");
				break;

			default:
				break;
			}
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
