package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Intent;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.controller.utils.TakePicture;
import com.integreight.onesheeld.shields.fragments.CameraFragment.CameraFragmentHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class CameraShield extends ControllerParent<CameraShield> implements
		CameraFragmentHandler {
	// private CameraEventHandler eventHandler;
	private static final byte CAMERA_COMMAND = (byte) 0x15;
	private static final byte CAPTURE_METHOD_ID = (byte) 0x01;
	private static final byte FLASH_METHOD_ID = (byte) 0x02;
	private static String FLASH_MODE;
	private static final byte FRONT_CAPTURE = (byte) 0x03;

	public CameraShield() {

	}

	public CameraShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<CameraShield> setTag(String tag) {

		return super.setTag(tag);
	}

	public void setCameraEventHandler(CameraEventHandler eventHandler) {
		// this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {

		if (frame.getShieldId() == CAMERA_COMMAND) {

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

				Intent translucent = new Intent(getApplication(),
						TakePicture.class);
				translucent.putExtra("FLASH", FLASH_MODE);
				translucent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(translucent);
				break;
			case FRONT_CAPTURE:
				Intent front_translucent = new Intent(getApplication(),
						TakePicture.class);
				front_translucent.putExtra("Front_Request", true);
				front_translucent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(front_translucent);

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraFragmentIntilized() {

	}

}
