package com.integreight.onesheeld.shields.controller;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.utils.ControllerParent;

public class EmailShield extends ControllerParent<EmailShield> {

	private EmailEventHandler eventHandler;
	private static final byte EMAIL_COMMAND = (byte) 0x1E;
	private static final byte SEND_METHOD_ID = (byte) 0x01;
	private boolean isLoggedIn = false;
	private String userEmail = "iabdelgawaad@gmail.com";
	private String password = "********";

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
				//sendMail(email_send_to, subject, body);
				sendMailUsingJavaAPI(email_send_to, subject, body);
			}
		}

	}

	public void setUserasLoggedIn(String userEmail , String password) {
		isLoggedIn = true;
		this.userEmail = userEmail;
		this.password = password;
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

	private Session createSessionObject() {
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		return Session.getInstance(properties, new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userEmail, password);}
	
		});
		}

	private Message createMessage(String email, String subject,
			String messageBody, Session session) throws MessagingException,
			UnsupportedEncodingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("iabdelgawaad@gmail.com",
				"Hema"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				email));
		message.setSubject(subject);
		message.setText(messageBody);
		return message;
	}

	private class SendMailTask extends AsyncTask<Message, Void, Void> {

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			Log.d("Email Sheeld:: SendMailTask ", "");

		}

		@Override
		protected Void doInBackground(Message... messages) {
			try {
				Transport.send(messages[0]);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private void sendMailUsingJavaAPI(String email, String subject,
			String messageBody) {
		Session session = createSessionObject();

		try {
			Message message = createMessage(email, subject, messageBody,
					session);
			new SendMailTask().execute(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
