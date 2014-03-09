package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;

public class GmailSinginPopup extends Dialog {
	private AlertDialog.Builder builder;
	private Activity activity;
	private LayoutInflater inflater;
	EditText userName_edt, password_edt;
	Button login_bt, cancel_bt;
	private String userName, password;
	TextView invalide_username_tx , invalide_password_tx;
	private EmailEventHandler emailEventHandler;

	public GmailSinginPopup(Activity context, EmailEventHandler eventHandler) {
		super(context, R.style.FullHeightDialog);
		this.activity = context;
		this.emailEventHandler = eventHandler;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gmail_singin_dialog_layout);
		inflater = activity.getLayoutInflater();
		builder = new AlertDialog.Builder(activity);
		// Set other dialog properties

		login_bt = (Button) findViewById(R.id.gmail_login_dialog);
		cancel_bt = (Button) findViewById(R.id.gmail_cancel_dialog);
		invalide_username_tx = (TextView) findViewById(R.id.gmail_signin_dialog_invalide_username_tx);
		invalide_password_tx = (TextView) findViewById(R.id.gmail_signin_dialog_invalide_password_tx);
		userName_edt = (EditText) findViewById(R.id.gmail_signin_dialog_username);
		password_edt = (EditText) findViewById(R.id.gmail_signin_dialog_password);

		userName_edt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				invalide_username_tx.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		password_edt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				invalide_password_tx.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		login_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// validate on username and password
				userName = userName_edt.getText().toString();
				password = password_edt.getText().toString();

				if (userName.contains(" ") || userName.contains("@")|| userName.equals(""))
				{
					invalide_username_tx.setVisibility(View.VISIBLE);
				}
				else if (password.equals("") || password.contains(" ") || password.length() < 8)
				{
					invalide_password_tx.setVisibility(View.VISIBLE);
				}
				else
					isUsernameValide(true);
			}
		});
		cancel_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		// Create the AlertDialog
		builder.create();
	}

	public void isUsernameValide(boolean isValide) {
		if (isValide) {
			userName = userName_edt.getText().toString();
			password = password_edt.getText().toString();
			emailEventHandler.onLoginSuccess(userName+"@gmail.com", password);
			cancel();
		} 
	}

}
