package com.integreight.onesheeld;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class OneSheeldVersionInstallerPopup extends Dialog {
	private Activity activity;

	public OneSheeldVersionInstallerPopup(Activity context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.activity = context;
	}

	public static boolean isOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.initialization_view);
		setCancelable(false);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				isOpened = false;
			}
		});
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		isOpened = true;
		super.onStart();
	}

}
