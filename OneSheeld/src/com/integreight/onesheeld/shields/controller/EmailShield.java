package com.integreight.onesheeld.shields.controller;

import java.util.Calendar;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;

import com.google.android.gms.common.AccountPicker;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.utils.ControllerParent;

public class EmailShield extends ControllerParent<EmailShield> {

	private EmailEventHandler eventHandler;
	private static final byte EMAIL_COMMAND = (byte) 0x1E;
	private static final byte SEND_METHOD_ID = (byte) 0x01;

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
			//addGoogleAccount(getActivity());
		}

	}
	
	public static void addGoogleAccount(final Activity activity) {
	    final AccountManager accountMgr = AccountManager.get(activity);
	    accountMgr.addAccount("com.google", "my_auth_token", null, null, activity, null, null);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
