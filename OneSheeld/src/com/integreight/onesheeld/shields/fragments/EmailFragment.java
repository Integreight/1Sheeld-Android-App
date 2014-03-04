package com.integreight.onesheeld.shields.fragments;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.EmailShield;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class EmailFragment extends ShieldFragmentParent<EmailFragment> {

	private static final int SOME_REQUEST_CODE = 0;
	private static final int RESULT_OK = 0;
	TextView sendTo, subject;
	private boolean isAccountpickered = false;

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
		Log.d("Email Sheeld::OnActivityCreated()", "");

		sendTo = (TextView) getView().findViewById(
				R.id.gmail_shield_sendto_textview);
		subject = (TextView) getView().findViewById(
				R.id.gmail_shield_subject_textview);
		

		subject.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getActivity().startActivityForResult(intent, SOME_REQUEST_CODE);

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
			((EmailShield) getApplication().getRunningShields().get(
					getControllerTag()))
					.setEmailEventHandler(emailEventHandler);
		}

	}

	public void addAccount() {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
		new String[] { "com.google" }, false, null, null, null, null);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		super.getAppActivity().startActivityForResult(intent, SOME_REQUEST_CODE);
	}
	

	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {
	//	super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SOME_REQUEST_CODE && resultCode == RESULT_OK) {
			if (isAccountpickered) {
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				Log.d("account name ", accountName);
			}
			isAccountpickered = true;
		}
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}
}
