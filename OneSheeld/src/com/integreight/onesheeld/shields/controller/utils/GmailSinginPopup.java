package com.integreight.onesheeld.shields.controller.utils;

import com.integreight.onesheeld.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GmailSinginPopup extends Dialog {
	private AlertDialog.Builder builder;
	private Activity activity;
	private  LayoutInflater inflater ;
	EditText userName_edt, password_edt;
	Button login_bt , cancel_bt;
	private String userName , password;
	
	public GmailSinginPopup(Activity context) {
		super(context,R.style.FullHeightDialog);
		this.activity = context;

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
		
		login_bt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//validate on username and password
				userName = userName_edt.getText().toString();	
				password = password_edt.getText().toString();
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
	

}
