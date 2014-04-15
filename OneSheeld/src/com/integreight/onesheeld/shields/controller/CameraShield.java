package com.integreight.onesheeld.shields.controller;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.TakePicture;
import com.integreight.onesheeld.shields.fragments.CameraFragment.CameraFragmentHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class CameraShield extends ControllerParent<CameraShield> implements
		CameraFragmentHandler {
	// private CameraEventHandler eventHandler;
	// private static final byte CAMERA_COMMAND = (byte) 0x15;
	private static final byte CAPTURE_METHOD_ID = (byte) 0x01;
	private static final byte FLASH_METHOD_ID = (byte) 0x02;
	private static String FLASH_MODE;
	private static final byte FRONT_CAPTURE = (byte) 0x03;
	private Queue<CameraCapture> cameraCaptureQueue;
	int numberOfFrames = 0;
	Handler UIHandler;
	boolean isFrontCamera = false;
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String message = intent.getStringExtra("message");
			Log.d("receiver", "Got message: " + message);

			while (cameraCaptureQueue.peek() != null
					&& cameraCaptureQueue.peek().isTaken())
				cameraCaptureQueue.poll();
			if (!cameraCaptureQueue.isEmpty()) {
				if (cameraCaptureQueue.peek().isFront())
					sendFrontCaptureImageIntent(cameraCaptureQueue.poll());
				else
					sendCaptureImageIntent(cameraCaptureQueue.poll());
			}
		}

	};

	private void sendCaptureImageIntent(CameraCapture camCapture) {
		if (camCapture != null) {
			if (!camCapture.isTaken()) {
				Intent translucent = new Intent(getApplication(),
						TakePicture.class);
				translucent.putExtra("FLASH", camCapture.getFlash());
				translucent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(translucent);
				camCapture.setTaken();
			}
		}
	}

	private void sendFrontCaptureImageIntent(CameraCapture camCapture) {
		if (camCapture != null) {
			if (!camCapture.isTaken()) {
				Intent front_translucent = new Intent(getApplication(),
						TakePicture.class);
				front_translucent.putExtra("Front_Request", true);
				front_translucent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(front_translucent);
				camCapture.setTaken();
			}
		}
	}

	public CameraShield() {
		UIHandler = new Handler();
		cameraCaptureQueue = new LinkedList<CameraShield.CameraCapture>();
	}

	public CameraShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<CameraShield> setTag(String tag) {
		LocalBroadcastManager.getInstance(
				getApplication().getApplicationContext()).registerReceiver(
				mMessageReceiver, new IntentFilter("custom-event-name"));
		return super.setTag(tag);
	}

	public void setCameraEventHandler(CameraEventHandler eventHandler) {
		// this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {

		if (frame.getShieldId() == UIShield.CAMERA_SHIELD.getId()) {

			// String userId = frame.getArgumentAsString(0);

			switch (frame.getFunctionId()) {
			case FLASH_METHOD_ID:
				byte flash_mode = frame.getArgument(0)[0];
				switch (flash_mode) {
				case 0:
					FLASH_MODE = "off";
					break;
				case 1:
					FLASH_MODE = "on";
					break;
				case 2:
					FLASH_MODE = "auto";
					break;
				default:
					break;
				}
				break;

			case CAPTURE_METHOD_ID:
				numberOfFrames++;
				Log.d("Camera", "Frames number = " + numberOfFrames);
				CameraCapture camCapture = new CameraCapture(FLASH_MODE, false);
				if (cameraCaptureQueue.isEmpty()) {
					sendCaptureImageIntent(camCapture);
				}
				cameraCaptureQueue.add(camCapture);
				break;
			case FRONT_CAPTURE:
				numberOfFrames++;
				Log.d("Camera", "Frames number front = " + numberOfFrames);
				CameraCapture frontCamCapture = new CameraCapture(FLASH_MODE,
						true);
				if (cameraCaptureQueue.isEmpty()) {
					sendFrontCaptureImageIntent(frontCamCapture);
				}
				cameraCaptureQueue.add(frontCamCapture);
				break;

			default:
				break;
			}
		}

	}

	public static interface CameraEventHandler {
		void OnPictureTaken();

		void checkCameraHardware(boolean isHasCamera);

		void takePicture();

		void setFlashMode(String flash_mode);
	}

	@Override
	public void reset() {
		LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(
				mMessageReceiver);
	}

	@Override
	public void onCameraFragmentIntilized() {

	}

	private class CameraCapture {
		private String flash;
		private boolean isTaken;
		private boolean isFrontCamera;

		public CameraCapture(String flash, boolean isFront) {
			this.flash = flash;
			isTaken = false;
			isFrontCamera = isFront;
		}

		public String getFlash() {
			return flash;
		}

		public boolean isTaken() {
			return isTaken;
		}

		public void setTaken() {
			isTaken = true;
		}

		public boolean isFront() {
			return isFrontCamera;

		}

	}

}
