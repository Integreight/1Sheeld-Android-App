package com.integreight.onesheeld.shields.controller.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;

public class GMailSender extends javax.mail.Authenticator {
	private String mailhost = "smtp.gmail.com";
	private static String user;
	private static String password;
	private Session session;
	// EmailEventHandler emailEventHandler;

	static {
		Security.addProvider(new JSSEProvider());
	}

	public GMailSender(String user_email, String pass) {
		user = user_email;
		password = pass;
		// this.emailEventHandler = handler;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		session = Session.getDefaultInstance(props, this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized int sendMail(String subject, String body,
			String sender, String recipients) {
		try {
			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(
					body.getBytes(), "text/plain"));
			message.setSender(new InternetAddress(sender));
			message.setSubject(subject);
			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else

				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));

			Transport.send(message);
			// emailEventHandler.onSuccess();
			return 0;
		} catch (AuthenticationFailedException e) {
			// emailEventHandler.onSendingAuthError("Authentication Failed");
			return 1;
		} catch (AddressException e) {
			// emailEventHandler.onEmailnotSent("message could not be sent to the recipient");
			return 2;
		} catch (SendFailedException e) {
			// emailEventHandler.onEmailnotSent("message could not be sent to the recipient ");
			return 2;
		} catch (MessagingException e) {
			// emailEventHandler.onEmailnotSent("message could not be sent to the recipient ");
			return 2;
		}
	}

	public class ByteArrayDataSource implements DataSource {
		private byte[] data;
		private String type;

		public ByteArrayDataSource(byte[] data, String type) {
			super();
			this.data = data;
			this.type = type;
		}

		public ByteArrayDataSource(byte[] data) {
			super();
			this.data = data;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContentType() {
			if (type == null)
				return "application/octet-stream";
			else
				return type;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getName() {
			return "ByteArrayDataSource";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Not Supported");
		}
	}
}