package com.integreight.onesheeld.shields.controller.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraService extends Service {

	// Camera variables
	// a surface holder
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters
	private Parameters parameters;
	private Bitmap bmp;
	FileOutputStream fo;
	private String FLASH_MODE;
	private int QUALITY_MODE = 0;
	private boolean isFrontCamRequest = false;
	private Camera.Size pictureSize;
	SurfaceView sv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();

	}

	private Camera openFrontFacingCameraGingerbread() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e("Camera",
							"Camera failed to open: " + e.getLocalizedMessage());
					/*
					 * Toast.makeText(getApplicationContext(),
					 * "Front Camera failed to open", Toast.LENGTH_LONG)
					 * .show();
					 */
				}
			}
		}
		return cam;
	}

	private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea > resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	/** Check if this device has a camera */
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

	/** Check if this device has front camera */
	private boolean checkFrontCamera(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FRONT)) {
			// this device has front camera
			return true;
		} else {
			// no front camera on this device
			return false;
		}
	}

	private class TakeImage extends AsyncTask<Intent, Void, Integer> {
		int result;

		@Override
		protected Integer doInBackground(Intent... params) {
			result = takeImage(params[0]);
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
			case 1:

				Toast.makeText(getApplicationContext(),
						"Your Device dosen't have Front Camera !",
						Toast.LENGTH_LONG).show();

				break;
			case 2:
				Toast.makeText(getApplicationContext(),
						"Your Device dosen't have a Camera !",
						Toast.LENGTH_LONG).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(),
						"API dosen't support front camera", Toast.LENGTH_LONG)
						.show();
				break;
			case 4:

				Toast.makeText(getApplicationContext(),
						"Your Picture has been taken !", Toast.LENGTH_LONG)
						.show();
				break;

			default:
				break;
			}
			super.onPostExecute(result);
		}
	}

	private int takeImage(Intent intent) {

		if (checkCameraHardware(getApplicationContext())) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String flash_mode = extras.getString("FLASH");
				FLASH_MODE = flash_mode;

				boolean front_cam_req = extras.getBoolean("Front_Request");
				isFrontCamRequest = front_cam_req;

				int quality_mode = extras.getInt("Quality_Mode");
				QUALITY_MODE = quality_mode;
			}

			if (isFrontCamRequest) {

				// set flash 0ff
				FLASH_MODE = "off";
				// only for gingerbread and newer versions
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
					mCamera = openFrontFacingCameraGingerbread();
					if (mCamera != null) {

						try {
							mCamera.setPreviewDisplay(sv.getHolder());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							stopSelf();
							return 3;
						}
						parameters = mCamera.getParameters();
						parameters.setFlashMode(FLASH_MODE);
						pictureSize = getBiggesttPictureSize(parameters);
						if (pictureSize != null)
							parameters.setPictureSize(pictureSize.width,
									pictureSize.height);
						// set camera parameters
						mCamera.setParameters(parameters);
						mCamera.startPreview();
						mCamera.takePicture(null, null, mCall);
						return 4;

					} else {
						mCamera = null;
						stopSelf();
						return 1;
					}
					/*
					 * sHolder = sv.getHolder(); // tells Android that this
					 * surface will have its data // constantly // replaced if
					 * (Build.VERSION.SDK_INT < 11)
					 * 
					 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
					 */
				} else {
					if (checkFrontCamera(getApplicationContext())) {
						mCamera = openFrontFacingCameraGingerbread();

						if (mCamera != null) {

							try {
								mCamera.setPreviewDisplay(sv.getHolder());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								stopSelf();
								return 3;
							}
							parameters = mCamera.getParameters();
							parameters.setFlashMode(FLASH_MODE);
							pictureSize = getBiggesttPictureSize(parameters);
							if (pictureSize != null)
								parameters.setPictureSize(pictureSize.width,
										pictureSize.height);
							// set camera parameters
							mCamera.setParameters(parameters);
							mCamera.startPreview();
							mCamera.takePicture(null, null, mCall);
							return 4;

						} else {
							mCamera = null;
							/*
							 * Toast.makeText(getApplicationContext(),
							 * "API dosen't support front camera",
							 * Toast.LENGTH_LONG).show();
							 */
							stopSelf();
							return 1;
						}
						// Get a surface
						/*
						 * sHolder = sv.getHolder(); // tells Android that this
						 * surface will have its data // constantly // replaced
						 * if (Build.VERSION.SDK_INT < 11)
						 * 
						 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
						 * );
						 */
					}

				}

			} else {

				if (mCamera != null) {
					mCamera.stopPreview();
					mCamera.release();
					mCamera = Camera.open();
				} else
					mCamera = getCameraInstance();

				try {
					mCamera.setPreviewDisplay(sv.getHolder());
					parameters = mCamera.getParameters();
					if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
						FLASH_MODE = "auto";
					}
					parameters.setFlashMode(FLASH_MODE);

					// set camera parameters
					mCamera.setParameters(parameters);
					mCamera.startPreview();
					mCamera.takePicture(null, null, mCall);
					return 4;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Get a surface
				/*
				 * sHolder = sv.getHolder(); // tells Android that this surface
				 * will have its data constantly // replaced if
				 * (Build.VERSION.SDK_INT < 11)
				 * 
				 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				 */

			}

		} else {
			// display in long period of time
			/*
			 * Toast.makeText(getApplicationContext(),
			 * "Your Device dosen't have a Camera !", Toast.LENGTH_LONG)
			 * .show();
			 */
			return 2;

		}
		return 0;

		// return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sv = new SurfaceView(getApplicationContext());

		new TakeImage().execute(intent);
		return 0;
	}

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// decode the data obtained by the camera into a Bitmap
			Log.d("ImageTakin", "Done");

			bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			if (bmp != null && QUALITY_MODE == 0)
				bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
			if (bmp != null && QUALITY_MODE != 0)
				bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);

			File imagesFolder = new File(
					Environment.getExternalStorageDirectory(), "OneSheeld");
			if (!imagesFolder.exists())
				imagesFolder.mkdirs(); // <----
			File image = new File(imagesFolder, System.currentTimeMillis()
					+ ".jpg");

			// write the bytes in file
			try {
				fo = new FileOutputStream(image);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
			}
			try {
				fo.write(bytes.toByteArray());
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

			// remember close de FileOutput
			try {
				fo.close();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));

			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			if (mCamera != null) {
				mCamera.stopPreview();
				// release the camera
				mCamera.release();
			}
			/*
			 * Toast.makeText(getApplicationContext(),
			 * "Your Picture has been taken !", Toast.LENGTH_LONG).show();
			 */
			com.integreight.onesheeld.Log.d("Camera", "Image Taken !");
			if (bmp != null) {
				bmp.recycle();
				bmp = null;
				System.gc();
			}
			mCamera = null;
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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

	@Override
	public void onDestroy() {
		Intent intent = new Intent("custom-event-name");
		// You can also include some extra data.
		intent.putExtra("message", "This is my message!");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

		super.onDestroy();
	}

}
