package com.integreight.onesheeld;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class FirmwareUpdatingPopup extends Dialog {

	public FirmwareUpdatingPopup(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.updating_firmware_view);
		super.onCreate(savedInstanceState);
	}
}
