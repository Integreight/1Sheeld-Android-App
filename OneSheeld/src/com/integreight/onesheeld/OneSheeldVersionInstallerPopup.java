package com.integreight.onesheeld;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
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
	Button button;
	Jodem jodem;
	Thread jodemThread;
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
		button = (Button) findViewById(R.id.bootloaderbutton);
		progressBar = (ProgressBar) findViewById(R.id.bootloaderProgressBar);
		textView = (TextView) findViewById(R.id.bootloaderUploadPercentage);
		progressBar.setProgress(0);
		progressBar.setMax(100);

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				jodemThread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							jodem.send(
									activity.getResources().openRawResource(
											R.raw.atmega_firmata), 3);
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
				firmata.prepareAppForSendingFirmware();
				button.setEnabled(false);
				jodemThread.start();

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
								button.setEnabled(true);
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
						if (jodemThread != null && jodemThread.isAlive()) {
							jodemThread.interrupt();
							jodemThread = null;
						}
						handler.post(new Runnable() {

							@Override
							public void run() {
								button.setEnabled(true);
								textView.setText(error);
							}
						});
					}

					@Override
					public void onTimout() {
						// TODO Auto-generated method stub
						if (jodemThread != null && jodemThread.isAlive()) {
							jodemThread.interrupt();
							jodemThread = null;
						}
						handler.post(new Runnable() {

							@Override
							public void run() {
								button.setEnabled(true);
								textView.setText("Timout Happened");
							}
						});
						
					}
				});
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (jodemThread != null && jodemThread.isAlive()) {
			jodemThread.interrupt();
			jodemThread = null;
		}
	}

}
