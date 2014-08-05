package com.integreight.onesheeld.shields.fragments;

import java.io.FileOutputStream;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.CameraShield.CameraEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class CameraFragment extends ShieldFragmentParent<CameraFragment> {
	// private SurfaceView sv;

	// a bitmap to display the captured image
	// private Bitmap bmp;
	FileOutputStream fo;

	// Camera variables
	// a surface holder
	// private SurfaceHolder sHolder;
	// a variable to control the camera
	// private Camera mCamera;
	// the camera parameters
	// private Parameters parameters;
	// private String FLASH_MODE = "on";
	private CameraFragmentHandler fragmentHandler;
	View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.camera_shield_fragment_layout, container,
				false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {

		super.onStart();

	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		fragmentHandler = new CameraFragmentHandler() {

			@Override
			public void onCameraFragmentIntilized() {
				// TODO Auto-generated method stub

			}
		};
		fragmentHandler.onCameraFragmentIntilized();
	}

	private CameraEventHandler cameraEventHandler = new CameraEventHandler() {

		@Override
		public void checkCameraHardware(boolean isHasCamera) {

			if (canChangeUI()) {
				if (!isHasCamera) {
					Toast.makeText(activity, "Your Device doesn't have Camera",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void OnPictureTaken() {
			if (canChangeUI())
				Toast.makeText(activity, "Your Camera has been Captured Image",
						Toast.LENGTH_SHORT).show();
		}

		@Override
		public void takePicture() {
		}

		@Override
		public void setFlashMode(String flash_mode) {

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new CameraShield(activity, getControllerTag()));

		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((CameraShield) getApplication().getRunningShields().get(
				getControllerTag())).setCameraEventHandler(cameraEventHandler);

	}

	public static interface CameraFragmentHandler {
		void onCameraFragmentIntilized();
	}
}
