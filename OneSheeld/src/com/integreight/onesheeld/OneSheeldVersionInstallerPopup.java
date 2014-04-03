package com.integreight.onesheeld;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public class OneSheeldVersionInstallerPopup extends Dialog {
	private Activity activity;
	ArduinoFirmata firmata;
	Button firmataButton;
	Button firmatarxtxButton;
	Button firmatausaButton;
	Button rxButton;
	Button txButton;
	Jodem jodem;
	ProgressBar progressBar;
	TextView textView;
	AsyncHttpClient httpClient;

	public OneSheeldVersionInstallerPopup(Activity context) {
		super(context, android.R.style.Theme_Black);
		this.activity = context;
	}

	public static boolean isOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.upgrading_firmware_dialog_layout);
		firmataButton = (Button) findViewById(R.id.firmatabootloaderbutton);
		firmatarxtxButton = (Button) findViewById(R.id.firmatabootloaderrxtxbutton);
		firmatausaButton = (Button) findViewById(R.id.firmatabootloaderusabutton);
		rxButton = (Button) findViewById(R.id.rxbootloaderbutton);
		txButton = (Button) findViewById(R.id.txbootloaderbutton);
		progressBar = (ProgressBar) findViewById(R.id.bootloaderProgressBar);
		textView = (TextView) findViewById(R.id.bootloaderUploadPercentage);
		httpClient=new AsyncHttpClient();
		progressBar.setProgress(0);
		progressBar.setMax(100);

		firmataButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				firmataButton.setEnabled(false);
				firmatarxtxButton.setEnabled(false);
				firmatausaButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				progressBar.setProgress(0);
				httpClient.get("http://www.1sheeld.com/api/firmware.json", new JsonHttpResponseHandler(){
					@Override
					public void onSuccess(JSONObject response) {
						// TODO Auto-generated method stub
						try {
							Log.d("bootloader",response.get("url").toString());
							textView.setText("Downloading...");
							httpClient.get(response.get("url").toString(),new BinaryHttpResponseHandler(new String[]{"application/octet-stream","text/plain"}){
								@Override
								public void onSuccess(byte[] binaryData) {
									// TODO Auto-generated method stub
									OneSheeldVersionInstallerPopup.this.setCancelable(false);
									firmata.prepareAppForSendingFirmware();
									firmata.resetMicro();
									jodem.send(binaryData, 4);
									textView.setText("Press reset now!");
									progressBar.setProgress(0);
									super.onSuccess(binaryData);
								}
								@Override
								public void onFailure(int statusCode,
										Header[] headers, byte[] binaryData,
										Throwable error) {
									// TODO Auto-generated method stub
									firmataButton.setEnabled(true);
									firmatarxtxButton.setEnabled(true);
									firmatausaButton.setEnabled(true);
									rxButton.setEnabled(true);
									txButton.setEnabled(true);
									textView.setText("Error Downloading!");
									Log.d("bootloader", statusCode+"");
									super.onFailure(statusCode, headers, binaryData, error);
								}
								@Override
								public void onProgress(int bytesWritten,
										int totalSize) {
									// TODO Auto-generated method stub
									progressBar.setProgress((int) ((bytesWritten/(float)totalSize)*100));
									super.onProgress(bytesWritten, totalSize);
								}
							});
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						super.onSuccess(response);
					};
				});

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
				firmatausaButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				progressBar.setProgress(0);
				firmata.resetMicro();
				jodem.send(activity.getResources().openRawResource(R.raw.atmega_firmata_pulsein_rxtx), 4);
				textView.setText("Press reset now!");
			}
		});
		firmatausaButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmataButton.setEnabled(false);
				firmatarxtxButton.setEnabled(false);
				firmatausaButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				progressBar.setProgress(0);
				firmata.resetMicro();
				jodem.send(activity.getResources().openRawResource(R.raw.atmega_firmata_usa_with_reset), 4);
				textView.setText("Press reset now!");
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
				firmatausaButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				progressBar.setProgress(0);
				firmata.resetMicro();
				jodem.send(activity.getResources().openRawResource(R.raw.onesheeld_rx_flash), 4);
				textView.setText("Press reset now!");
			}
		});
		txButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OneSheeldVersionInstallerPopup.this.setCancelable(false);
				firmata.prepareAppForSendingFirmware();
				firmatarxtxButton.setEnabled(false);
				firmatausaButton.setEnabled(false);
				firmataButton.setEnabled(false);
				rxButton.setEnabled(false);
				txButton.setEnabled(false);
				progressBar.setProgress(0);
				firmata.resetMicro();
				jodem.send(activity.getResources().openRawResource(R.raw.onesheeld_tx_flash), 4);
				textView.setText("Press reset now!");
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
								firmatausaButton.setEnabled(true);
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
								firmatausaButton.setEnabled(true);
								rxButton.setEnabled(true);
								txButton.setEnabled(true);
								textView.setText(error);
								OneSheeldVersionInstallerPopup.this.setCancelable(true);
								firmata.returnAppToNormal();
								firmata.enableReporting();
								firmata.setAllPinsAsInput();
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
								firmatausaButton.setEnabled(true);
								rxButton.setEnabled(true);
								txButton.setEnabled(true);
								textView.setText("Timout Happened");
								OneSheeldVersionInstallerPopup.this.setCancelable(true);
								firmata.returnAppToNormal();
								firmata.enableReporting();
								firmata.setAllPinsAsInput();
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
