package com.integreight.onesheeld.shields.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.EmailShield;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class EmailFragment extends ShieldFragmentParent<EmailFragment> {

	private static final int SOME_REQUEST_CODE = 0;
	TextView sendTo, subject , userName;
	Button login_bt, logout_bt;
	private static SharedPreferences mSharedPreferences;
	private static final String PREF_EMAIL_SHIELD_USER_LOGIN = "user_login_status";
	private static final String PREF_EMAIL_SHIELD_GMAIL_ACCOUNT = "gmail_account";
	private String userEmail;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.email_shield_fragment_layout,
				container, false);
		return v;
	}

	@Override
	public void onStart() {

		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(false);

		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		((EmailShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setEmailEventHandler(emailEventHandler);
	
		Log.d("Email Sheeld::OnActivityCreated()", "");

		sendTo = (TextView) getView().findViewById(
				R.id.gmail_shield_sendto_textview);
		userName = (TextView) getView().findViewById(R.id.gmail_shield_username_textview);
		subject = (TextView) getView().findViewById(
				R.id.gmail_shield_subject_textview);
		login_bt = (Button) getView().findViewById(R.id.login_gmail_bt);
		logout_bt = (Button) getView().findViewById(R.id.logout_gmail_bt);
		login_bt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addAccount();
			}
		});
		mSharedPreferences = getActivity().getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);

		// if user logged in run controller else ask for login
		if (isGmailLoggedInAlready()) {
			userEmail = mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
			((EmailShield) getApplication().getRunningShields().get(
					getControllerTag())).setUserasLoggedIn(userEmail,"");
			userName.setVisibility(View.VISIBLE);
			userName.setText(userEmail);
			logout_bt.setVisibility(View.VISIBLE);
		} else {
			login_bt.setVisibility(View.VISIBLE);
		}
		
		logout_bt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				logoutGmailAccount();	

			}
		});

	}

	public boolean isGmailLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_EMAIL_SHIELD_USER_LOGIN,
				false);
	}

	private EmailEventHandler emailEventHandler = new EmailEventHandler() {

		@Override
		public void onSendingError(String error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEmailsent(String email_send_to, String subject_text) {
			if (canChangeUI()) {
				sendTo.setText(email_send_to);
				subject.setText(subject_text);
			} else {

			}

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new EmailShield(getActivity(), getControllerTag()));
		}
	}

	public void addAccount() {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, SOME_REQUEST_CODE);
	}

	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {

		if (requestCode == SOME_REQUEST_CODE
				&& resultCode == FragmentActivity.RESULT_OK) {
			String accountName = data
					.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			Log.d("account name ", accountName);
			// save in share perefrences
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, accountName);
			editor.putBoolean(PREF_EMAIL_SHIELD_USER_LOGIN, true);

			// Commit the edits!
			editor.commit();
			((EmailShield) getApplication().getRunningShields().get(
					getControllerTag())).setUserasLoggedIn(accountName,"");
			login_bt.setVisibility(View.INVISIBLE);
			logout_bt.setVisibility(View.VISIBLE);
			userName.setVisibility(View.VISIBLE);
			userName.setText(accountName);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}
	
	private void logoutGmailAccount() {
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT);
		e.remove(PREF_EMAIL_SHIELD_USER_LOGIN);
		e.commit();
		login_bt.setVisibility(View.VISIBLE);
		logout_bt.setVisibility(View.INVISIBLE);
		userName.setVisibility(View.INVISIBLE);
		subject.setText("");
		sendTo.setText("");


	}
}
