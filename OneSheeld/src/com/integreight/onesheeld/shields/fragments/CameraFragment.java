package com.integreight.onesheeld.shields.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.CameraShield.CameraEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class CameraFragment extends ShieldFragmentParent<CameraFragment>
		implements SurfaceHolder.Callback {
	private SurfaceView sv;

	// a bitmap to display the captured image
	private Bitmap bmp;
	FileOutputStream fo;

	// Camera variables
	// a surface holder
	private SurfaceHolder sHolder;
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters
	private Parameters parameters;
	private String FLASH_MODE = "on";
	private CameraFragmentHandler fragmentHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.camera_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {

		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);

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

			if (!isHasCamera) {
				Toast.makeText(getActivity(),
						"Your Device doesn't have Camera", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void OnPictureTaken() {
			Toast.makeText(getActivity(),
					"Your Camera has been Captured Image", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void takePicture() {
			captureImage();
		}

		@Override
		public void setFlashMode(String flash_mode) {

			FLASH_MODE = flash_mode;
		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new CameraShield(getActivity(), getControllerTag()));

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

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

		// get camera parameters
		parameters = mCamera.getParameters();
		parameters.setFlashMode(FLASH_MODE);

		// set camera parameters
		mCamera.setParameters(parameters);
		mCamera.startPreview();

		// sets what code should be executed after the picture is taken
		Camera.PictureCallback mCall = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// decode the data obtained by the camera into a Bitmap
				Log.d("ImageTakin", "Done");

				bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

				File imagesFolder = new File(
						Environment.getExternalStorageDirectory(), "OneSheeld");
				imagesFolder.mkdirs(); // <----
				File image = new File(imagesFolder, System.currentTimeMillis()
						+ ".jpg");

				// write the bytes in file
				try {
					fo = new FileOutputStream(image);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					fo.write(bytes.toByteArray());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// remember close de FileOutput
				try {
					fo.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mCamera.stopPreview();
				// release the camera
				mCamera.release();

			}
		};

		mCamera.takePicture(null, null, mCall);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = getCameraInstance();
		try {
			mCamera.setPreviewDisplay(holder);

		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera = null;

	}
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private void captureImage() {
		// check if this device has a camera
		if (checkCameraHardware(getActivity())) {

			sv = (SurfaceView) this.getView().findViewById(R.id.camera_preview);

			// Get a surface
			sHolder = this.sv.getHolder();

			// add the callback interface methods defined below as the Surface
			// View
			// callbacks
			sHolder.addCallback(this);

			// tells Android that this surface will have its data constantly
			// replaced
			sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		} else {
			// display in long period of time
			Toast.makeText(getActivity(),
					"Your Device dosen't have a Camera !", Toast.LENGTH_LONG)
					.show();
		}
	}

	public static interface CameraFragmentHandler {
		void onCameraFragmentIntilized ();
	}
}
