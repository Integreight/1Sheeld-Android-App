package com.integreight.onesheeld;

import com.integreight.onesheeld.utils.OneShieldButton;
import com.integreight.onesheeld.utils.OneShieldTextView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class FirmwareUpdatingPopup extends Dialog {
	private OneShieldButton upgradeBtn;
	private ProgressBar progress;
	private OneShieldTextView statusText;
	private MainActivity activity;
	private RelativeLayout transactionSlogan;

	public FirmwareUpdatingPopup(MainActivity activity) {
		super(activity, android.R.style.Theme_Translucent_NoTitleBar);
		this.activity = activity;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.updating_firmware_view);
		upgradeBtn = (OneShieldButton) findViewById(R.id.update);
		progress = (ProgressBar) findViewById(R.id.progressUpdating);
		statusText = (OneShieldTextView) findViewById(R.id.updateStatusText);
		transactionSlogan = (RelativeLayout) findViewById(R.id.transactionSloganUpdating);
		setUpgrade();
		changeSlogan(
				activity.getResources().getString(R.string.upgradeFirmata),
				COLOR.BLUE);
		super.onCreate(savedInstanceState);
	}

	private void setUpgrade() {
		upgradeBtn.setText("Upgrade");
		upgradeBtn.setVisibility(View.VISIBLE);
		progress.setVisibility(View.INVISIBLE);
	}

	private void changeSlogan(String text, int color) {
		statusText.setText(text);
		transactionSlogan.setBackgroundColor(color);
	}

	private final static class COLOR {
		public final static int RED = 0xff9B1201;
		public final static int YELLOW = 0xffE79401;
		public final static int BLUE = 0xff0094C1;
		public final static int ORANGE = 0xffE74D01;
	}
}
