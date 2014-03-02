package com.integreight.onesheeld;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.Jodem;

public class OneSheeldVersionInstallerPopup extends Dialog {
	private Activity activity;
	ArduinoFirmata firmata;
	Button firmataButton;
	Button firmatarxtxButton;
	Button rxButton;
	Button txButton;
	Jodem jodem;
	ProgressBar progressBar;
	TextView textView;

	public OneSheeldVersionInstallerPopup(Activity context) {
		super(context, android.R.style.Theme_Black);
		this.activity = context;
	}

	public static boolean isOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.upgrading_firmware_dialog_layout);
		firmataButton = (Button) findViewById(R.id.firmatabootloaderbutton);
		firmataButton = (Button) findViewById(R.id.firmatabootloaderrxtxbutton);
		rxButton = (Button) findViewById(R.id.rxbootloaderbutton);
		txButton = (Button) findViewById(R.id.txbootloaderbutton);
		progressBar = (ProgressBar) findViewById(R.id.bootloaderProgressBar);
		textView = (TextView) findViewById(R.id.bootloaderUploadPercentage);
		progressBar.setProgress(0);
		progressBar.setMax(100);

		firmataButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmataButton.setEnabled(false);
				firmatarxtxButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				jodem.send(activity.getResources().openRawResource(R.raw.atmega_firmata), 3);

			}
		});
		firmatarxtxButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmataButton.setEnabled(false);
				firmatarxtxButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				jodem.send(activity.getResources().openRawResource(R.raw.atmega_firmata_rxtx), 3);

			}
		});
		rxButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmataButton.setEnabled(false);
				firmatarxtxButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				jodem.send(activity.getResources().openRawResource(R.raw.onesheeld_rx_flash), 3);

			}
		});
		txButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmatarxtxButton.setEnabled(false);
				firmataButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				jodem.send(activity.getResources().openRawResource(R.raw.onesheeld_tx_flash), 3);

			}
		});
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
		firmata = ((OneSheeldApplication) activity.getApplication())
				.getAppFirmata();
		final Handler handler = new Handler();
		jodem = new Jodem(firmata.getBTService(),
				new Jodem.JodemEventHandler() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						firmata.returnAppToNormal();
						firmata.enableReporting();
						firmata.setAllPinsAsInput();
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								firmataButton.setEnabled(true);
								firmatarxtxButton.setEnabled(true);
								rxButton.setEnabled(true);
								txButton.setEnabled(true);
								OneSheeldVersionInstallerPopup.this.setCancelable(true);
							}
						});

					}

					@Override
					public void onProgress(final int totalBytes, int sendBytes,
							int errorCount) {
						// TODO Auto-generated method stub
						Log.d("bootloader", "total:" + totalBytes + " sent:"
								+ sendBytes + " error:" + errorCount);
						final int status = (int) ((float) sendBytes
								/ totalBytes * 100);
						handler.post(new Runnable() {

							@Override
							public void run() {
								progressBar.setProgress(status);
								textView.setText(status + "/"
										+ progressBar.getMax());

							}
						});

					}

					@Override
					public void onError(final String error) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								firmataButton.setEnabled(true);
								firmatarxtxButton.setEnabled(true);
								rxButton.setEnabled(true);
								txButton.setEnabled(true);
								textView.setText(error);
								OneSheeldVersionInstallerPopup.this.setCancelable(true);
							}
						});
					}

					@Override
					public void onTimout() {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {

							@Override
							public void run() {
								firmataButton.setEnabled(true);
								firmatarxtxButton.setEnabled(true);
								rxButton.setEnabled(true);
								txButton.setEnabled(true);
								textView.setText("Timout Happened");
								OneSheeldVersionInstallerPopup.this.setCancelable(true);
							}
						});
						
					}
				});
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		jodem.stop();
	}

}
